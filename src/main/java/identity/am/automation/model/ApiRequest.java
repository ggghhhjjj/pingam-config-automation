package identity.am.automation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import identity.am.automation.config.ConfigProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for all API requests
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ApiRequest {
    @JsonIgnore
    private String endpoint;

    @JsonIgnore
    private HttpMethod method;

    @JsonIgnore
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    @JsonIgnore
    @Builder.Default
    private Map<String, String> queryParams = new HashMap<>();

    /**
     * Get the request body for serialization
     * Default implementation returns this, subclasses can override to provide custom body
     */
    @JsonIgnore
    public Object getBody() {
        return this;
    }

    /**
     * Add a header to the request
     */
    public ApiRequest withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Add a query parameter to the request
     */
    public ApiRequest withQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    /**
     * Updates placeholders in the request with values from configuration properties
     * This method handles common fields (headers, endpoint, query params)
     * Subclasses should override to handle their specific fields
     *
     * @param configProperties The configuration properties to use for resolution
     * @return This request with placeholders updated
     */
    public ApiRequest updatePlaceholders(ConfigProperties configProperties) {
        // Update endpoint
        if (endpoint != null && endpoint.contains("${")) {
            endpoint = resolvePlaceholders(endpoint, configProperties);
        }

        // Update headers
        this.headers = headers.entrySet().stream()
                .map(entry -> resolveEntryPlaceholder(entry, configProperties))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        // Update query parameters
        this.queryParams = queryParams.entrySet().stream()
                .map(entry -> resolveEntryPlaceholder(entry, configProperties))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return this;
    }

    protected Map.Entry<String, String> resolveEntryPlaceholder(Map.Entry<String, String> entry, ConfigProperties configProperties) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (key != null && key.contains("${")) {
            key = resolvePlaceholders(key, configProperties);
        }

        if (value != null && value.contains("${")) {
            value = resolvePlaceholders(value, configProperties);
        }

        return new AbstractMap.SimpleEntry<>(key, value);
    }


    /**
     * Helper method to resolve placeholders in a string
     *
     * @param value The string containing placeholders
     * @param configProperties The configuration properties to use for resolution
     * @return The string with placeholders resolved
     */
    protected String resolvePlaceholders(String value, ConfigProperties configProperties) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        String result = value;
        int startIndex;
        while ((startIndex = result.indexOf("${")) != -1) {
            int endIndex = result.indexOf("}", startIndex);
            if (endIndex == -1) {
                break;
            }

            String placeholder = result.substring(startIndex + 2, endIndex);
            String propertyValue = configProperties.getProperty(placeholder);

            result = result.substring(0, startIndex) +
                    (propertyValue != null ? propertyValue : "") +
                    result.substring(endIndex + 1);
        }

        return result;
    }
}