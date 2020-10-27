package uk.co.gresearch.siembol.response.stream.rest.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("uk.co.gresearch.siembol")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setRegisterShutdownHook(true);
        application.run(args);
    }
}
