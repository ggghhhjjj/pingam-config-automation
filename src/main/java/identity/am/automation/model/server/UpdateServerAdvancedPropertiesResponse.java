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
 * Response for the update server advanced properties request
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateServerAdvancedPropertiesResponse extends ApiResponse {

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
     * Extract the updated LB cookie value from the response
     * @return The updated LB cookie value or null if not found
     */
    @JsonIgnore
    public Object getUpdatedLbCookieValue() {
        return properties.get("com.iplanet.am.lbcookie.value");
    }

    /**
     * Extract the updated replication port value from the response
     * @return The updated replication port value or null if not found
     */
    @JsonIgnore
    public Object getUpdatedReplicationPort() {
        return properties.get("com.sun.embedded.replicationport");
    }

    @Override
    public boolean isValid() {
        return properties != null && !properties.isEmpty();
    }
}