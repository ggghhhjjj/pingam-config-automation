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
 * Response for the get server advanced properties request
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetServerAdvancedPropertiesResponse extends ApiResponse {

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
     * Extract the LB cookie value from the response
     * @return The LB cookie value or null if not found
     */
    @JsonIgnore
    public Object getLbCookieValue() {
        return properties.get("com.iplanet.am.lbcookie.value");
    }

    /**
     * Extract the replication port value from the response
     * @return The replication port value or null if not found
     */
    @JsonIgnore
    public Object getReplicationPort() {
        return properties.get("com.sun.embedded.replicationport");
    }

    @Override
    public boolean isValid() {
        return properties != null && !properties.isEmpty();
    }
}
