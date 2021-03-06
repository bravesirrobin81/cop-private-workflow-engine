package uk.gov.homeoffice.borders.workflow.task

import com.fasterxml.jackson.core.type.TypeReference
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.Spin
import org.springframework.http.MediaType
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.task.comment.TaskComment

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CommentsApiControllerSpec extends BaseSpec {

    def processInstance

    void createTasks(number, assignee) {
        def tasks = []
        number.times {
            def data = new Data()
            data.assignee = assignee
            data.candidateGroup = "teamA"
            data.name = "test ${it}"
            data.description = "test ${it}"
            tasks << data
        }

        def objectValue =
                Spin.S(tasks, "application/json")

        def variables = [:]
        variables['collectionOfData'] = objectValue
        variables['type'] = 'non-notification'
        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables)
    }


    def 'can create comment'() {
        given:
        wireMockStub.stub {
            request {
                method 'POST'
                url '/comment'
            }

            response {
                status 201
                headers {
                    "Content-Type" "application/json"
                }

            }

        }

        and:
        createTasks(1, "test")
        and:
        logInUser()
        and:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list().first()
        and:
        def comment = new TaskComment()
        comment.taskId = task.id
        comment.comment = "message"
        comment.staffId = 'test'


        when:
        def result = mvc.perform(post("/api/workflow/tasks/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

        then:

        result.andExpect(status().is2xxSuccessful())
        def commentDto = objectMapper.readValue(result.andReturn().response.contentAsString, TaskComment)
        commentDto
        commentDto.comment == comment.comment
    }


    def 'can get comments for task'() {
        given:
        createTasks(1, "test")
        and:
        logInUser()
        and:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list().first()
        and:
        def comment = new TaskComment()
        comment.taskId = task.id
        comment.comment = "message"
        comment.staffId = 'test'

        and:
        wireMockStub.stub {
            request {
                method 'POST'
                url '/comment'
            }

            response {
                status 201
                headers {
                    "Content-Type" "application/json"
                }
            }

        }
        wireMockStub.stub {
            request {
                method 'GET'
                url '/comment?taskid=eq.' + task.id + '&order=createdon.desc'
            }
            response {
                status 201
                body """ [
                         {
                                "taskid" : "id",
                                "comment" : "message"
                              
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        and:
        mvc.perform(post("/api/workflow/tasks/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

        when:

        def result = mvc.perform(get("/api/workflow/tasks/${task.id}/comments")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        def comments = objectMapper.readValue(result.andReturn().response.contentAsString, new TypeReference<List<TaskComment>>() {
        })
        comments
        !comments.isEmpty()

    }

    def 'exception thrown if current user and comment user are not the same'() {
        given:
        wireMockStub.stub {
            request {
                method 'POST'
                url '/comment'
            }

            response {
                status 201
                headers {
                    "Content-Type" "application/json"
                }

            }

        }

        and:
        createTasks(1, "test")
        and:
        logInUser()
        and:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list().first()
        and:
        def comment = new TaskComment()
        comment.taskId = task.id
        comment.comment = "message"
        comment.staffId = 'testACS'


        when:
        def result = mvc.perform(post("/api/workflow/tasks/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is4xxClientError())
    }

    def '404 thrown if task does not exist for comment'() {
        given:
        wireMockStub.stub {
            request {
                method 'POST'
                url '/comment'
            }

            response {
                status 201
                headers {
                    "Content-Type" "application/json"
                }

            }

        }

        and:
        createTasks(1, "test")
        and:
        logInUser()

        and:
        def comment = new TaskComment()
        comment.taskId = "randomTaskId"
        comment.comment = "message"
        comment.staffId = 'testACS'


        when:
        def result = mvc.perform(post("/api/workflow/tasks/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is4xxClientError())
    }

    def 'throws 4xx error if comment does not have message'() {
        given:
        createTasks(1, "test")
        and:
        logInUser()
        and:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list().first()
        and:
        def comment = new TaskComment()
        comment.taskId = task.id
        comment.staffId = 'test'


        when:
        def result = mvc.perform(post("/api/workflow/tasks/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is4xxClientError())

    }
}
