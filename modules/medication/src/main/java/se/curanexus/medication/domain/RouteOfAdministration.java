package se.curanexus.medication.domain;

/**
 * Administreringsväg för läkemedel.
 * Baserat på svenska/nordiska standarder.
 */
public enum RouteOfAdministration {
    /** Oral (via munnen) */
    ORAL,

    /** Sublingual (under tungan) */
    SUBLINGUAL,

    /** Rektal (via ändtarmen) */
    RECTAL,

    /** Intravenös (i blodbanan) */
    INTRAVENOUS,

    /** Intramuskulär (i muskeln) */
    INTRAMUSCULAR,

    /** Subkutan (under huden) */
    SUBCUTANEOUS,

    /** Inhalation (via luftvägarna) */
    INHALATION,

    /** Nasal (via näsan) */
    NASAL,

    /** Topikal (på huden) */
    TOPICAL,

    /** Transdermal (genom huden, t.ex. plåster) */
    TRANSDERMAL,

    /** Ögondroppar */
    OPHTHALMIC,

    /** Örondroppar */
    OTIC,

    /** Vaginal */
    VAGINAL,

    /** Epidural */
    EPIDURAL,

    /** Intratekal (ryggmärgsvätskan) */
    INTRATHECAL,

    /** Annan */
    OTHER
}
