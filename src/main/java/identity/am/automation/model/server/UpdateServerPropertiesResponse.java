package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import identity.am.automation.model.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Response for the update server properties request
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateServerPropertiesResponse extends ApiResponse {

    @JsonIgnore
    private Map<String, Object> properties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Extract the updated site value from the response
     * @return The updated site value or "[Empty]" if not found
     */
    @JsonIgnore
    public String getUpdatedSiteValue() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> siteHeader = (Map<String, Object>) properties.get("amconfig.header.site");
            if (siteHeader != null) {
                return (String) siteHeader.get("singleChoiceSite");
            }
        } catch (ClassCastException e) {
            // Handle potential type mismatches
        }
        return "[Empty]";
    }

    @Override
    public boolean isValid() {
        return properties != null && !properties.isEmpty();
    }
}