package se.curanexus.medication.adapter.fass;

/**
 * Exception for Fass API errors.
 * Used to wrap API-specific errors and provide meaningful messages.
 */
public class FassApiException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public FassApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public FassApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = null;
    }

    public FassApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public FassApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Check if this is a "not found" error.
     * Fass returns SMPC_DOCUMENT_NOT_FOUND_BECAUSE_NO_SWEDISH_SMPC_EXISTS for some products.
     */
    public boolean isNotFound() {
        return statusCode == 404 ||
               "SMPC_DOCUMENT_NOT_FOUND_BECAUSE_NO_SWEDISH_SMPC_EXISTS".equals(errorCode);
    }

    /**
     * Check if this is an authentication error.
     */
    public boolean isAuthenticationError() {
        return statusCode == 401 || statusCode == 403;
    }

    /**
     * Check if this is a rate limiting error.
     */
    public boolean isRateLimited() {
        return statusCode == 429;
    }
}
