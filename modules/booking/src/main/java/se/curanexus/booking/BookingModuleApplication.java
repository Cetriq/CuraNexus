package se.curanexus.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingModuleApplication.class, args);
    }
}
