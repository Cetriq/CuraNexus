package se.curanexus.encounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"se.curanexus.encounter", "se.curanexus.events"})
public class CareEncounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareEncounterApplication.class, args);
    }
}
