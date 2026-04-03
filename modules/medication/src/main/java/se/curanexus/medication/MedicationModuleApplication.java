package se.curanexus.medication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"se.curanexus.medication", "se.curanexus.events"})
public class MedicationModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicationModuleApplication.class, args);
    }
}
