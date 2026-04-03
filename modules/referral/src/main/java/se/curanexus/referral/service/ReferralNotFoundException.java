package se.curanexus.referral.service;

import java.util.UUID;

public class ReferralNotFoundException extends RuntimeException {

    public ReferralNotFoundException(UUID referralId) {
        super("Remiss hittades inte: " + referralId);
    }

    public ReferralNotFoundException(String message) {
        super(message);
    }
}
