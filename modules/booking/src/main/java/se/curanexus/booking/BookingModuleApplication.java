package se.curanexus.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"se.curanexus.booking", "se.curanexus.events"})
@EnableScheduling
public class BookingModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingModuleApplication.class, args);
    }
}
