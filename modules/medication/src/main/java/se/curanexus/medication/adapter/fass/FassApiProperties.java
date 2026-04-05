package se.curanexus.medication.adapter.fass;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;

/**
 * Configuration properties for Fass API integration.
 *
 * Required: Agreement with Lif (fass@lif.se)
 * Documentation: https://api.fass.se/documentation
 */
@ConfigurationProperties(prefix = "curanexus.fass")
@Validated
public class FassApiProperties {

    /**
     * Fass API base URL.
     */
    @NotBlank
    private String baseUrl = "https://api.fass.se";

    /**
     * API key for Fass (provided by Lif).
     */
    private String apiKey;

    /**
     * Client ID for JWT authentication.
     */
    private String clientId;

    /**
     * Client secret for JWT authentication.
     */
    private String clientSecret;

    /**
     * Connection timeout.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * Read timeout.
     */
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * Enable/disable the integration.
     * Useful for development/testing without Fass credentials.
     */
    private boolean enabled = true;

    /**
     * Cache TTL for product information.
     * Fass data updates every 30 minutes.
     */
    private Duration cacheTtl = Duration.ofMinutes(15);

    // Getters and setters

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}
