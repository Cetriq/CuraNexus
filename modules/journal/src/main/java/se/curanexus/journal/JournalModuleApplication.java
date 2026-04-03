package se.curanexus.journal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"se.curanexus.journal", "se.curanexus.events"})
public class JournalModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(JournalModuleApplication.class, args);
    }
}
