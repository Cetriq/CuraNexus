package se.curanexus.lab.domain;

/**
 * Status för enskilt testresultat.
 */
public enum ResultStatus {
    /** Väntar på analys */
    PENDING,

    /** Preliminärt resultat */
    PRELIMINARY,

    /** Slutgiltigt resultat */
    FINAL,

    /** Korrigerat resultat */
    CORRECTED,

    /** Makulerat resultat */
    CANCELLED,

    /** Analys misslyckades */
    FAILED
}
