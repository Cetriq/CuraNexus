package se.curanexus.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"se.curanexus.lab", "se.curanexus.events"})
public class LabModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabModuleApplication.class, args);
    }
}
