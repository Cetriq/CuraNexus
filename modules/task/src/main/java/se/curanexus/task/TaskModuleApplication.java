package se.curanexus.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"se.curanexus.task", "se.curanexus.events"})
public class TaskModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskModuleApplication.class, args);
    }
}
