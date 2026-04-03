package se.curanexus.medication.api.dto;

import se.curanexus.medication.domain.DosageForm;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.util.UUID;

public record MedicationDto(
        UUID id,
        String nplId,
        String atcCode,
        String name,
        String genericName,
        String manufacturer,
        String strength,
        BigDecimal strengthValue,
        String strengthUnit,
        DosageForm dosageForm,
        RouteOfAdministration route,
        Integer packageSize,
        String packageUnit,
        boolean narcotic,
        String narcoticClass,
        boolean prescriptionRequired,
        boolean substitutable,
        String fullDescription
) {
    public static MedicationDto from(Medication m) {
        return new MedicationDto(
                m.getId(),
                m.getNplId(),
                m.getAtcCode(),
                m.getName(),
                m.getGenericName(),
                m.getManufacturer(),
                m.getStrength(),
                m.getStrengthValue(),
                m.getStrengthUnit(),
                m.getDosageForm(),
                m.getRoute(),
                m.getPackageSize(),
                m.getPackageUnit(),
                m.isNarcotic(),
                m.getNarcoticClass(),
                m.isPrescriptionRequired(),
                m.isSubstitutable(),
                m.getFullDescription()
        );
    }
}
