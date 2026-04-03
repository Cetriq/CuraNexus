package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.medication.api.dto.InteractionCheckResult;
import se.curanexus.medication.domain.MedicationInteraction;
import se.curanexus.medication.domain.MedicationInteraction.InteractionSeverity;
import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.domain.PrescriptionStatus;
import se.curanexus.medication.repository.MedicationInteractionRepository;
import se.curanexus.medication.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service för interaktionskontroll mellan läkemedel.
 */
@Service
@Transactional(readOnly = true)
public class InteractionCheckService {

    private static final Logger log = LoggerFactory.getLogger(InteractionCheckService.class);

    private final MedicationInteractionRepository interactionRepository;
    private final PrescriptionRepository prescriptionRepository;

    public InteractionCheckService(MedicationInteractionRepository interactionRepository,
                                    PrescriptionRepository prescriptionRepository) {
        this.interactionRepository = interactionRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Kontrollera interaktioner för en ny ordination mot patientens befintliga ordinationer.
     */
    public InteractionCheckResult checkInteractionsForNewPrescription(UUID patientId, String newAtcCode) {
        log.info("Checking interactions for patient {} with new ATC code {}", patientId, newAtcCode);

        if (newAtcCode == null || newAtcCode.isBlank()) {
            return InteractionCheckResult.noInteractions();
        }

        // Hämta patientens aktiva ordinationer
        List<Prescription> activePrescriptions = prescriptionRepository.findActiveByPatientIdOnDate(
                patientId, LocalDate.now());

        if (activePrescriptions.isEmpty()) {
            return InteractionCheckResult.noInteractions();
        }

        // Samla ATC-koder
        Set<String> existingAtcCodes = activePrescriptions.stream()
                .map(this::getAtcCodeFromPrescription)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (existingAtcCodes.isEmpty()) {
            return InteractionCheckResult.noInteractions();
        }

        // Kolla interaktioner
        return checkInteractionsBetweenAtcCodes(newAtcCode, existingAtcCodes);
    }

    /**
     * Kontrollera interaktioner mellan en ATC-kod och en mängd andra ATC-koder.
     */
    public InteractionCheckResult checkInteractionsBetweenAtcCodes(String atcCode, Set<String> otherAtcCodes) {
        List<MedicationInteraction> allInteractions = new ArrayList<>();

        for (String otherAtc : otherAtcCodes) {
            List<MedicationInteraction> interactions = interactionRepository.findByAtcCodePair(atcCode, otherAtc);
            allInteractions.addAll(interactions);

            // Kolla även på substansgruppnivå (3-tecken ATC-prefix)
            if (atcCode.length() >= 3 && otherAtc.length() >= 3) {
                String prefix1 = atcCode.substring(0, 3);
                String prefix2 = otherAtc.substring(0, 3);
                if (!prefix1.equals(atcCode) || !prefix2.equals(otherAtc)) {
                    interactions = interactionRepository.findByAtcCodePair(prefix1, prefix2);
                    allInteractions.addAll(interactions);
                }
            }
        }

        // Ta bort dubbletter
        List<MedicationInteraction> uniqueInteractions = allInteractions.stream()
                .collect(Collectors.toMap(
                        MedicationInteraction::getId,
                        i -> i,
                        (a, b) -> a
                ))
                .values()
                .stream()
                .toList();

        return InteractionCheckResult.from(uniqueInteractions);
    }

    /**
     * Kontrollera alla interaktioner för en patients läkemedelslista.
     */
    public InteractionCheckResult checkAllPatientInteractions(UUID patientId) {
        log.info("Checking all interactions for patient {}", patientId);

        List<Prescription> activePrescriptions = prescriptionRepository.findActiveByPatientIdOnDate(
                patientId, LocalDate.now());

        if (activePrescriptions.size() < 2) {
            return InteractionCheckResult.noInteractions();
        }

        // Samla alla ATC-koder
        List<String> atcCodes = activePrescriptions.stream()
                .map(this::getAtcCodeFromPrescription)
                .filter(Objects::nonNull)
                .toList();

        if (atcCodes.size() < 2) {
            return InteractionCheckResult.noInteractions();
        }

        // Hämta alla interaktioner för dessa ATC-koder
        List<MedicationInteraction> interactions = interactionRepository.findByAtcCodes(atcCodes);

        // Filtrera till de som faktiskt är mellan patientens läkemedel
        List<MedicationInteraction> relevantInteractions = interactions.stream()
                .filter(i -> isInteractionBetweenPatientMedications(i, atcCodes))
                .toList();

        return InteractionCheckResult.from(relevantInteractions);
    }

    /**
     * Kontrollera specifik interaktion mellan två ATC-koder.
     */
    public InteractionCheckResult checkInteractionBetween(String atc1, String atc2) {
        List<MedicationInteraction> interactions = interactionRepository.findByAtcCodePair(atc1, atc2);
        return InteractionCheckResult.from(interactions);
    }

    /**
     * Hämta endast allvarliga interaktioner för patient.
     */
    public InteractionCheckResult checkSevereInteractions(UUID patientId) {
        List<Prescription> activePrescriptions = prescriptionRepository.findActiveByPatientIdOnDate(
                patientId, LocalDate.now());

        if (activePrescriptions.size() < 2) {
            return InteractionCheckResult.noInteractions();
        }

        List<String> atcCodes = activePrescriptions.stream()
                .map(this::getAtcCodeFromPrescription)
                .filter(Objects::nonNull)
                .toList();

        List<MedicationInteraction> interactions = interactionRepository.findByAtcCodesAndSeverity(
                atcCodes,
                List.of(InteractionSeverity.CONTRAINDICATED, InteractionSeverity.SEVERE)
        );

        List<MedicationInteraction> relevantInteractions = interactions.stream()
                .filter(i -> isInteractionBetweenPatientMedications(i, atcCodes))
                .toList();

        return InteractionCheckResult.from(relevantInteractions);
    }

    private String getAtcCodeFromPrescription(Prescription prescription) {
        if (prescription.getAtcCode() != null) {
            return prescription.getAtcCode();
        }
        if (prescription.getMedication() != null) {
            return prescription.getMedication().getAtcCode();
        }
        return null;
    }

    private boolean isInteractionBetweenPatientMedications(MedicationInteraction interaction, List<String> patientAtcCodes) {
        String atc1 = interaction.getAtcCode1();
        String atc2 = interaction.getAtcCode2();

        boolean hasAtc1 = patientAtcCodes.stream().anyMatch(atc -> matchesAtcCode(atc, atc1));
        boolean hasAtc2 = patientAtcCodes.stream().anyMatch(atc -> matchesAtcCode(atc, atc2));

        return hasAtc1 && hasAtc2;
    }

    private boolean matchesAtcCode(String patientAtc, String interactionAtc) {
        if (patientAtc == null || interactionAtc == null) {
            return false;
        }
        // Exakt match eller prefix-match (för substansgrupp-baserade interaktioner)
        return patientAtc.equals(interactionAtc) || patientAtc.startsWith(interactionAtc);
    }
}
