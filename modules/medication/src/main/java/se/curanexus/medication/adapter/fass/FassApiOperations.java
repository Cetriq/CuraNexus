package se.curanexus.medication.adapter.fass;

import se.curanexus.medication.adapter.fass.dto.FassAtcCode;
import se.curanexus.medication.adapter.fass.dto.FassMedicinalProduct;
import se.curanexus.medication.adapter.fass.dto.FassProductDocument;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Fass API operations.
 * Allows for easy mocking in tests.
 */
public interface FassApiOperations {

    /**
     * Get medicinal product by NPL-ID.
     */
    Optional<FassMedicinalProduct> getMedicinalProduct(String nplId);

    /**
     * Search medicinal products by name or ATC code.
     */
    List<FassMedicinalProduct> searchProducts(String query, int limit);

    /**
     * Get products by ATC code.
     */
    List<FassMedicinalProduct> getProductsByAtcCode(String atcCode);

    /**
     * Get products by ATC group.
     */
    List<FassMedicinalProduct> getProductsByAtcGroup(String atcGroup);

    /**
     * Get SMPC (Summary of Product Characteristics).
     */
    Optional<FassProductDocument> getSmpc(String nplId);

    /**
     * Get PIL (Patient Information Leaflet).
     */
    Optional<FassProductDocument> getPil(String nplId);

    /**
     * Get ATC classification tree.
     */
    List<FassAtcCode> getAtcCodes();
}
