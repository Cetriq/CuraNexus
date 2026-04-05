package se.curanexus.medication.adapter.fass;

import org.springframework.stereotype.Component;
import se.curanexus.medication.adapter.fass.dto.FassActiveSubstance;
import se.curanexus.medication.adapter.fass.dto.FassMedicinalProduct;
import se.curanexus.medication.adapter.fass.dto.FassPackage;
import se.curanexus.medication.domain.DosageForm;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Mapper for converting Fass API responses to domain model.
 * Follows REQ-MED-061: No business logic in adapters.
 */
@Component
public class FassProductMapper {

    /**
     * Map Fass medicinal product to domain Medication entity.
     *
     * @param fassProduct Product from Fass API
     * @return Domain Medication entity
     */
    public Medication toDomain(FassMedicinalProduct fassProduct) {
        if (fassProduct == null) {
            return null;
        }

        String genericName = extractGenericName(fassProduct);
        Medication medication = new Medication(fassProduct.name(), genericName);

        // NPL identifiers
        medication.setNplId(fassProduct.nplId());

        // ATC classification
        medication.setAtcCode(fassProduct.atcCode());

        // Manufacturer
        medication.setManufacturer(fassProduct.marketingAuthorizationHolder());

        // Strength
        medication.setStrength(fassProduct.strengthText());
        if (fassProduct.strengthNumeric() != null) {
            medication.setStrengthValue(BigDecimal.valueOf(fassProduct.strengthNumeric()));
        }
        medication.setStrengthUnit(fassProduct.strengthUnit());

        // Dosage form
        medication.setDosageForm(mapDosageForm(fassProduct.pharmaceuticalForm()));

        // Route of administration (derived from dosage form if not explicit)
        medication.setRoute(deriveRouteFromForm(fassProduct.pharmaceuticalForm()));

        // First package info (if available)
        if (fassProduct.packages() != null && !fassProduct.packages().isEmpty()) {
            FassPackage firstPackage = fassProduct.packages().get(0);
            medication.setNplPackId(firstPackage.nplPackId());
            medication.setPackageSize(firstPackage.packageSize());
            medication.setPackageUnit(firstPackage.packageSizeUnit());
        }

        // Prescription requirements
        medication.setPrescriptionRequired(
                fassProduct.prescriptionRequired() != null && fassProduct.prescriptionRequired()
        );

        // Narcotic classification
        medication.setNarcotic(
                fassProduct.narcotic() != null && fassProduct.narcotic()
        );
        medication.setNarcoticClass(fassProduct.narcoticClass());

        // Substitutability
        medication.setSubstitutable(
                fassProduct.substitutable() == null || fassProduct.substitutable()
        );

        // Active status based on marketing status
        medication.setActive(isActiveOnMarket(fassProduct.marketingStatus()));

        return medication;
    }

    /**
     * Extract generic name from active substances.
     */
    private String extractGenericName(FassMedicinalProduct product) {
        if (product.activeSubstances() == null || product.activeSubstances().isEmpty()) {
            return null;
        }

        return product.activeSubstances().stream()
                .map(FassActiveSubstance::substanceName)
                .collect(Collectors.joining(" + "));
    }

    /**
     * Map Fass pharmaceutical form to domain DosageForm.
     */
    private DosageForm mapDosageForm(String pharmaceuticalForm) {
        if (pharmaceuticalForm == null) {
            return null;
        }

        String form = pharmaceuticalForm.toLowerCase();

        // Tablets
        if (form.contains("tablett") || form.contains("tablet")) {
            if (form.contains("brustablett") || form.contains("effervescent")) {
                return DosageForm.EFFERVESCENT_TABLET;
            }
            return DosageForm.TABLET;
        }

        // Capsules
        if (form.contains("kapsel") || form.contains("capsule")) {
            return DosageForm.CAPSULE;
        }

        // Oral solutions
        if (form.contains("oral lösning") || form.contains("oral solution") ||
            form.contains("sirap") || form.contains("syrup") ||
            form.contains("mixtur") || form.contains("oral suspension")) {
            return DosageForm.ORAL_SOLUTION;
        }

        // Injections
        if (form.contains("injektionsvätska") || form.contains("solution for injection")) {
            return DosageForm.INJECTION;
        }
        if (form.contains("infusionsvätska") || form.contains("solution for infusion")) {
            return DosageForm.INFUSION;
        }
        if (form.contains("pulver till injektionsvätska")) {
            return DosageForm.POWDER;
        }

        // Topical
        if (form.contains("kräm") || form.contains("cream")) {
            return DosageForm.CREAM;
        }
        if (form.contains("salva") || form.contains("ointment")) {
            return DosageForm.OINTMENT;
        }
        if (form.contains("gel")) {
            return DosageForm.GEL;
        }
        if (form.contains("plåster") || form.contains("patch")) {
            return DosageForm.PATCH;
        }

        // Inhalation
        if (form.contains("inhalationsspray") || form.contains("inhalationspulver") ||
            form.contains("inhalation")) {
            return DosageForm.INHALER;
        }

        // Nasal
        if (form.contains("nässpray") || form.contains("nasal spray")) {
            return DosageForm.NASAL_SPRAY;
        }

        // Rectal
        if (form.contains("suppositorium") || form.contains("suppository")) {
            return DosageForm.SUPPOSITORY;
        }

        // Eye
        if (form.contains("ögondroppar") || form.contains("eye drops")) {
            return DosageForm.EYE_DROPS;
        }

        // Ear
        if (form.contains("örondroppar") || form.contains("ear drops")) {
            return DosageForm.EAR_DROPS;
        }

        // Vaginal
        if (form.contains("vaginal")) {
            return DosageForm.VAGINAL;
        }

        // Granules
        if (form.contains("granulat") || form.contains("granules")) {
            return DosageForm.GRANULES;
        }

        // Powder
        if (form.contains("pulver") || form.contains("powder")) {
            return DosageForm.POWDER;
        }

        return DosageForm.OTHER;
    }

    /**
     * Derive route of administration from pharmaceutical form.
     * Note: Order matters - more specific matches must come before general ones.
     */
    private RouteOfAdministration deriveRouteFromForm(String pharmaceuticalForm) {
        if (pharmaceuticalForm == null) {
            return null;
        }

        String form = pharmaceuticalForm.toLowerCase();

        // Ophthalmic - check before generic "droppar"
        if (form.contains("ögon") || form.contains("eye")) {
            return RouteOfAdministration.OPHTHALMIC;
        }

        // Otic (ear) - check before generic "droppar"
        if (form.contains("öron") || form.contains("ear")) {
            return RouteOfAdministration.OTIC;
        }

        // Nasal
        if (form.contains("näs") || form.contains("nasal")) {
            return RouteOfAdministration.NASAL;
        }

        // Oral - generic "droppar" comes last in the oral check
        if (form.contains("tablett") || form.contains("kapsel") ||
            form.contains("oral") || form.contains("sirap") ||
            form.contains("mixtur") || form.contains("droppar")) {
            return RouteOfAdministration.ORAL;
        }

        // Injection routes
        if (form.contains("intravenös") || form.contains("infusion")) {
            return RouteOfAdministration.INTRAVENOUS;
        }
        if (form.contains("intramuskulär")) {
            return RouteOfAdministration.INTRAMUSCULAR;
        }
        if (form.contains("subkutan")) {
            return RouteOfAdministration.SUBCUTANEOUS;
        }
        if (form.contains("injektionsvätska") || form.contains("injection")) {
            return RouteOfAdministration.SUBCUTANEOUS; // Default for unspecified injection
        }

        // Topical
        if (form.contains("kräm") || form.contains("salva") ||
            form.contains("gel") || form.contains("kutant")) {
            return RouteOfAdministration.TOPICAL;
        }
        if (form.contains("plåster") || form.contains("patch")) {
            return RouteOfAdministration.TRANSDERMAL;
        }

        // Inhalation
        if (form.contains("inhalation")) {
            return RouteOfAdministration.INHALATION;
        }

        // Rectal
        if (form.contains("suppositorium") || form.contains("rektal")) {
            return RouteOfAdministration.RECTAL;
        }

        return null;
    }

    /**
     * Determine if product is active on market.
     */
    private boolean isActiveOnMarket(String marketingStatus) {
        if (marketingStatus == null) {
            return true; // Assume active if not specified
        }

        String status = marketingStatus.toLowerCase();
        return !status.contains("avregistrerad") &&
               !status.contains("withdrawn") &&
               !status.contains("inactive");
    }
}
