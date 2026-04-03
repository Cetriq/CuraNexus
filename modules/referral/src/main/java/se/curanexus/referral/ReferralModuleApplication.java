package se.curanexus.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"se.curanexus.referral", "se.curanexus.events"})
public class ReferralModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferralModuleApplication.class, args);
    }
}
