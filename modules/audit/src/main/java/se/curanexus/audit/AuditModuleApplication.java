package se.curanexus.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AuditModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditModuleApplication.class, args);
    }
}
