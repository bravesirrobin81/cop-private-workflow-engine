package uk.gov.homeoffice.borders;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BordersWorkflowEngineApplication {

    public static void main(String[] args) {
        log.info("Starting borders workflow engine....");
        SpringApplication.run(BordersWorkflowEngineApplication.class, args);
    }
}
