package se.curanexus.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.referral.domain.ReferralResponse;
import se.curanexus.referral.domain.ReferralResponseType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralResponseRepository extends JpaRepository<ReferralResponse, UUID> {

    List<ReferralResponse> findByReferralIdOrderByCreatedAtDesc(UUID referralId);

    List<ReferralResponse> findByReferralIdAndResponseType(UUID referralId, ReferralResponseType responseType);

    @Query("SELECT rr FROM ReferralResponse rr WHERE rr.referral.id = :referralId " +
            "AND rr.responseType = 'FINAL_RESPONSE' " +
            "ORDER BY rr.createdAt DESC")
    Optional<ReferralResponse> findLatestFinalResponse(@Param("referralId") UUID referralId);

    @Query("SELECT rr FROM ReferralResponse rr WHERE rr.referral.id = :referralId " +
            "AND rr.responseType = 'ACCEPTANCE'")
    Optional<ReferralResponse> findAcceptanceResponse(@Param("referralId") UUID referralId);

    @Query("SELECT rr FROM ReferralResponse rr WHERE rr.referral.id = :referralId " +
            "AND rr.responseType = 'REJECTION'")
    Optional<ReferralResponse> findRejectionResponse(@Param("referralId") UUID referralId);

    List<ReferralResponse> findByResponderIdOrderByCreatedAtDesc(UUID responderId);

    @Query("SELECT COUNT(rr) FROM ReferralResponse rr WHERE rr.referral.id = :referralId")
    long countByReferralId(@Param("referralId") UUID referralId);
}
