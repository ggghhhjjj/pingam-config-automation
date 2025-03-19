package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Request for updating server properties
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateServerPropertiesRequest extends ApiRequest {

    @JsonIgnore
    protected Map<String, Object> properties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Override the getBody method to use the properties map as the body
     */
    @Override
    @JsonIgnore
    public Object getBody() {
        return properties;
    }

    /**
     * Create a default request for updating server properties with all existing properties preserved
     *
     * @return A request with placeholders that will be resolved at runtime
     */
    public static UpdateServerPropertiesRequest createDefault() {
        UpdateServerPropertiesRequest request = (UpdateServerPropertiesRequest) UpdateServerPropertiesRequest.builder()
                .endpoint("/json/global-config/servers/${server.id}/properties/general")
                .method(HttpMethod.PUT)
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0");

        // Initialize the properties map to prevent NullPointerException
        request.properties = new HashMap<>();

        return request;
    }

    @Override
    public ApiRequest updatePlaceholders(ConfigProperties configProperties) {
        // Call parent implementation to handle common fields
        super.updatePlaceholders(configProperties);

        // Ensure properties map is initialized
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }

        // Get the site ID to update
        String siteId = configProperties.getProperty("site.id");

        // Get the stored server properties JSON from the previous call
        String serverPropertiesJson = configProperties.getProperty("server.properties.json");

        if (serverPropertiesJson != null && !serverPropertiesJson.isEmpty()) {
            try {
                // Parse the stored properties using Jackson
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.properties = objectMapper.readValue(serverPropertiesJson,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                // Update the site value while preserving all other properties
                @SuppressWarnings("unchecked")
                Map<String, Object> siteHeader = (Map<String, Object>) properties.getOrDefault("amconfig.header.site", new HashMap<>());
                siteHeader.put("singleChoiceSite", siteId);
                properties.put("amconfig.header.site", siteHeader);
            } catch (Exception e) {
                // If there's an error, fall back to just setting the site property
                Map<String, Object> siteHeader = new HashMap<>();
                siteHeader.put("singleChoiceSite", siteId);
                properties.put("amconfig.header.site", siteHeader);
            }
        } else {
            // No properties stored, just set the site value
            Map<String, Object> siteHeader = new HashMap<>();
            siteHeader.put("singleChoiceSite", siteId);
            properties.put("amconfig.header.site", siteHeader);
        }

        return this;
    }
}