package se.curanexus.triage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TriagePriority")
class TriagePriorityTest {

    @Test
    @DisplayName("should have correct colors")
    void shouldHaveCorrectColors() {
        assertEquals("Red", TriagePriority.IMMEDIATE.getColor());
        assertEquals("Orange", TriagePriority.EMERGENT.getColor());
        assertEquals("Yellow", TriagePriority.URGENT.getColor());
        assertEquals("Green", TriagePriority.LESS_URGENT.getColor());
        assertEquals("Blue", TriagePriority.NON_URGENT.getColor());
    }

    @Test
    @DisplayName("should have correct max wait times")
    void shouldHaveCorrectMaxWaitTimes() {
        assertEquals(0, TriagePriority.IMMEDIATE.getMaxWaitMinutes());
        assertEquals(15, TriagePriority.EMERGENT.getMaxWaitMinutes());
        assertEquals(60, TriagePriority.URGENT.getMaxWaitMinutes());
        assertEquals(120, TriagePriority.LESS_URGENT.getMaxWaitMinutes());
        assertEquals(240, TriagePriority.NON_URGENT.getMaxWaitMinutes());
    }

    @Test
    @DisplayName("isHigherThan should return true for higher priority")
    void isHigherThanShouldReturnTrueForHigherPriority() {
        assertTrue(TriagePriority.IMMEDIATE.isHigherThan(TriagePriority.EMERGENT));
        assertTrue(TriagePriority.IMMEDIATE.isHigherThan(TriagePriority.NON_URGENT));
        assertTrue(TriagePriority.EMERGENT.isHigherThan(TriagePriority.URGENT));
        assertTrue(TriagePriority.URGENT.isHigherThan(TriagePriority.LESS_URGENT));
        assertTrue(TriagePriority.LESS_URGENT.isHigherThan(TriagePriority.NON_URGENT));
    }

    @Test
    @DisplayName("isHigherThan should return false for same priority")
    void isHigherThanShouldReturnFalseForSamePriority() {
        assertFalse(TriagePriority.IMMEDIATE.isHigherThan(TriagePriority.IMMEDIATE));
        assertFalse(TriagePriority.URGENT.isHigherThan(TriagePriority.URGENT));
    }

    @Test
    @DisplayName("isHigherThan should return false for lower priority")
    void isHigherThanShouldReturnFalseForLowerPriority() {
        assertFalse(TriagePriority.NON_URGENT.isHigherThan(TriagePriority.IMMEDIATE));
        assertFalse(TriagePriority.URGENT.isHigherThan(TriagePriority.EMERGENT));
        assertFalse(TriagePriority.LESS_URGENT.isHigherThan(TriagePriority.URGENT));
    }
}
