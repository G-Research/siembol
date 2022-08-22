package uk.co.gresearch.siembol.deployment.monitoring.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("uk.co.gresearch.siembol.deployment.monitoring")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setRegisterShutdownHook(true);
        app.run(args);
    }
}
