package se.curanexus.lab.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabSpecimen - Domänmodell")
class LabSpecimenTest {

    @Test
    @DisplayName("Ska skapa prov med typ")
    void shouldCreateSpecimenWithType() {
        LabSpecimen specimen = new LabSpecimen(SpecimenType.BLOOD_VENOUS);

        assertEquals(SpecimenType.BLOOD_VENOUS, specimen.getSpecimenType());
        assertNotNull(specimen.getCreatedAt());
        assertFalse(specimen.getRejected());
    }

    @Test
    @DisplayName("Ska skapa prov med streckkod")
    void shouldCreateSpecimenWithBarcode() {
        LabSpecimen specimen = new LabSpecimen(SpecimenType.URINE, "BC123456");

        assertEquals(SpecimenType.URINE, specimen.getSpecimenType());
        assertEquals("BC123456", specimen.getBarcode());
    }

    @Test
    @DisplayName("Ska registrera provtagning")
    void shouldRegisterCollection() {
        LabSpecimen specimen = new LabSpecimen(SpecimenType.BLOOD_VENOUS);
        UUID collectorId = UUID.randomUUID();

        specimen.collect(collectorId, "Sjuksköterska Nilsson");

        assertEquals(collectorId, specimen.getCollectorId());
        assertEquals("Sjuksköterska Nilsson", specimen.getCollectorName());
        assertNotNull(specimen.getCollectedAt());
    }

    @Test
    @DisplayName("Ska registrera mottagning på lab")
    void shouldRegisterLabReceipt() {
        LabSpecimen specimen = new LabSpecimen(SpecimenType.BLOOD_VENOUS);
        specimen.collect(UUID.randomUUID(), "Test");

        specimen.receiveAtLab();

        assertNotNull(specimen.getReceivedAtLab());
    }

    @Test
    @DisplayName("Ska kunna avvisa prov")
    void shouldRejectSpecimen() {
        LabSpecimen specimen = new LabSpecimen(SpecimenType.BLOOD_VENOUS);

        specimen.reject("Hemolyserat prov");

        assertTrue(specimen.getRejected());
        assertEquals("Hemolyserat prov", specimen.getRejectionReason());
    }
}
