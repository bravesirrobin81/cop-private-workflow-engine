package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    private RestTemplate restTemplate;
    private RefDataUrlBuilder refDataUrlBuilder;

    public Team findById(String teamId) {
        ResponseEntity<List<Team>> response = restTemplate.exchange(refDataUrlBuilder.teamById(teamId),
                HttpMethod.GET, httpEntity(), new ParameterizedTypeReference<List<Team>>() {
                });
        return response.getStatusCode().is2xxSuccessful() && !response.getBody().isEmpty() ? response.getBody().get(0) : null;

    }

    public List<Group> findByQuery(TeamQuery query) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<Team> teams = restTemplate.exchange(
                refDataUrlBuilder.teamQuery(query),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<List<Team>>() {
                },
                new HashMap<>()
        ).getBody();
        return new ArrayList<>(teams);
    }

    public List<Team> teamChildren(String teamId) {
        return teamChildren(singleton(teamId));
    }

    public List<Team> teamChildren(Collection<String> teamId) {
        if (teamId.isEmpty()) {
            return emptyList();
        }
        List<Team> teams = ofNullable(restTemplate
                .exchange(refDataUrlBuilder.teamChildren(teamId),
                        HttpMethod.GET,
                        httpEntity(),
                        new ParameterizedTypeReference<List<Team>>() {}).getBody()).orElse(new ArrayList<>());

        final Collection<String> childIds = teams.stream().map(Team::getId).collect(toSet());
        teams.addAll(teamChildren(childIds));
        return teams;
    }

    private HttpEntity httpEntity() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/vnd.pgrst.object+json");
        return new HttpEntity(httpHeaders);
    }
}
