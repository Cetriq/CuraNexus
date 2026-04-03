package se.curanexus.lab.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabOrder - Domänmodell")
class LabOrderTest {

    private UUID patientId;
    private UUID orderingUnitId;
    private UUID orderingPractitionerId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        orderingUnitId = UUID.randomUUID();
        orderingPractitionerId = UUID.randomUUID();
    }

    private LabOrder createOrder() {
        LabOrder order = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);
        order.addOrderItem(new LabOrderItem("NPU01685", "NPU", "Hemoglobin", SpecimenType.BLOOD_VENOUS));
        return order;
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa beställning med DRAFT-status")
        void shouldCreateOrderWithDraftStatus() {
            LabOrder order = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);

            assertEquals(LabOrderStatus.DRAFT, order.getStatus());
            assertEquals(LabOrderPriority.ROUTINE, order.getPriority());
            assertNotNull(order.getOrderReference());
            assertTrue(order.getOrderReference().startsWith("LAB-"));
            assertNotNull(order.getCreatedAt());
        }

        @Test
        @DisplayName("Ska generera unik beställningsreferens")
        void shouldGenerateUniqueOrderReference() {
            LabOrder order1 = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);
            LabOrder order2 = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);

            assertNotEquals(order1.getOrderReference(), order2.getOrderReference());
        }
    }

    @Nested
    @DisplayName("Skicka beställning")
    class SendOrder {

        @Test
        @DisplayName("Ska kunna skicka beställning med tester")
        void shouldSendOrderWithTests() {
            LabOrder order = createOrder();

            order.send();

            assertEquals(LabOrderStatus.ORDERED, order.getStatus());
            assertNotNull(order.getOrderedAt());
        }

        @Test
        @DisplayName("Ska ej kunna skicka tom beställning")
        void shouldNotSendEmptyOrder() {
            LabOrder order = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);

            assertThrows(IllegalStateException.class, order::send);
        }

        @Test
        @DisplayName("Ska ej kunna skicka redan skickad beställning")
        void shouldNotSendAlreadySentOrder() {
            LabOrder order = createOrder();
            order.send();

            assertThrows(IllegalStateException.class, order::send);
        }
    }

    @Nested
    @DisplayName("Mottagning")
    class MarkReceived {

        @Test
        @DisplayName("Ska kunna markera beställning som mottagen")
        void shouldMarkOrderAsReceived() {
            LabOrder order = createOrder();
            order.send();

            order.markReceived();

            assertEquals(LabOrderStatus.RECEIVED, order.getStatus());
            assertNotNull(order.getReceivedAt());
        }

        @Test
        @DisplayName("Ska ej kunna markera draft som mottagen")
        void shouldNotMarkDraftAsReceived() {
            LabOrder order = createOrder();

            assertThrows(IllegalStateException.class, order::markReceived);
        }
    }

    @Nested
    @DisplayName("Provtagning")
    class SpecimenCollection {

        @Test
        @DisplayName("Ska kunna registrera provtagning")
        void shouldRegisterSpecimenCollection() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();

            order.markSpecimenCollected();

            assertEquals(LabOrderStatus.SPECIMEN_COLLECTED, order.getStatus());
            assertNotNull(order.getSpecimenCollectedAt());
        }

        @Test
        @DisplayName("Ska kunna registrera provtagning direkt efter skickad")
        void shouldRegisterSpecimenDirectlyAfterOrdered() {
            LabOrder order = createOrder();
            order.send();

            order.markSpecimenCollected();

            assertEquals(LabOrderStatus.SPECIMEN_COLLECTED, order.getStatus());
        }
    }

    @Nested
    @DisplayName("Analys")
    class Analysis {

        @Test
        @DisplayName("Ska kunna starta analys")
        void shouldStartAnalysis() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();
            order.markSpecimenCollected();

            order.startAnalysis();

            assertEquals(LabOrderStatus.IN_PROGRESS, order.getStatus());
        }

        @Test
        @DisplayName("Ska kunna registrera delresultat")
        void shouldRegisterPartialResults() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();
            order.markSpecimenCollected();
            order.startAnalysis();

            order.registerPartialResults();

            assertEquals(LabOrderStatus.PARTIAL_RESULTS, order.getStatus());
        }

        @Test
        @DisplayName("Ska kunna slutföra analys")
        void shouldCompleteAnalysis() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();
            order.markSpecimenCollected();
            order.startAnalysis();

            order.complete();

            assertEquals(LabOrderStatus.COMPLETED, order.getStatus());
            assertNotNull(order.getCompletedAt());
        }
    }

    @Nested
    @DisplayName("Makulera")
    class Cancel {

        @Test
        @DisplayName("Ska kunna makulera draft")
        void shouldCancelDraft() {
            LabOrder order = createOrder();

            order.cancel("Felaktigt skapad");

            assertEquals(LabOrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Ska kunna makulera skickad beställning")
        void shouldCancelSentOrder() {
            LabOrder order = createOrder();
            order.send();

            order.cancel("Patient avbokad");

            assertEquals(LabOrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna makulera slutförd beställning")
        void shouldNotCancelCompletedOrder() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();
            order.markSpecimenCollected();
            order.startAnalysis();
            order.complete();

            assertThrows(IllegalStateException.class, () -> order.cancel("Test"));
        }
    }

    @Nested
    @DisplayName("Avvisa")
    class Reject {

        @Test
        @DisplayName("Ska kunna avvisa mottagen beställning")
        void shouldRejectReceivedOrder() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();

            order.reject("Ogiltig diagnos");

            assertEquals(LabOrderStatus.REJECTED, order.getStatus());
        }

        @Test
        @DisplayName("Ska ej kunna avvisa påbörjad analys")
        void shouldNotRejectInProgressOrder() {
            LabOrder order = createOrder();
            order.send();
            order.markReceived();
            order.markSpecimenCollected();
            order.startAnalysis();

            assertThrows(IllegalStateException.class, () -> order.reject("Test"));
        }
    }

    @Nested
    @DisplayName("Hjälpmetoder")
    class HelperMethods {

        @Test
        @DisplayName("Ska returnera true för redigerbar draft")
        void shouldReturnTrueForEditableDraft() {
            LabOrder order = createOrder();

            assertTrue(order.isEditable());
        }

        @Test
        @DisplayName("Ska returnera false för icke-redigerbar status")
        void shouldReturnFalseForNonEditableStatus() {
            LabOrder order = createOrder();
            order.send();

            assertFalse(order.isEditable());
        }
    }

    @Nested
    @DisplayName("Order items")
    class OrderItems {

        @Test
        @DisplayName("Ska kunna lägga till test")
        void shouldAddOrderItem() {
            LabOrder order = new LabOrder(patientId, orderingUnitId, orderingPractitionerId);
            LabOrderItem item = new LabOrderItem("NPU01685", "Hemoglobin");

            order.addOrderItem(item);

            assertEquals(1, order.getOrderItems().size());
            assertEquals(order, item.getLabOrder());
        }

        @Test
        @DisplayName("Ska ej kunna lägga till test efter skickad")
        void shouldNotAddItemAfterSent() {
            LabOrder order = createOrder();
            order.send();

            assertThrows(IllegalStateException.class,
                    () -> order.addOrderItem(new LabOrderItem("TEST", "Test")));
        }
    }

    @Nested
    @DisplayName("Prover")
    class Specimens {

        @Test
        @DisplayName("Ska kunna lägga till prov")
        void shouldAddSpecimen() {
            LabOrder order = createOrder();
            LabSpecimen specimen = new LabSpecimen(SpecimenType.BLOOD_VENOUS, "BC123456");

            order.addSpecimen(specimen);

            assertEquals(1, order.getSpecimens().size());
            assertEquals(order, specimen.getLabOrder());
        }
    }
}
