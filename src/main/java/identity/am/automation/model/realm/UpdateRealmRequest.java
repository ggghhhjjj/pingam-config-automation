package identity.am.automation.model.realm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Request for updating a realm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRealmRequest extends ApiRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("aliases")
    private List<String> aliases;

    /**
     * Create a default request for updating a realm with given ID
     */
    public static UpdateRealmRequest createDefault(String realmId, String realmName, boolean active,
                                                   String parentPath, List<String> aliases) {
        return (UpdateRealmRequest) UpdateRealmRequest.builder()
                .endpoint("/json/global-config/realms/" + realmId)
                .method(HttpMethod.PUT)
                .name(realmName)
                .active(active)
                .parentPath(parentPath != null ? parentPath : "")
                .aliases(aliases)
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=2.0,resource=1.0")
                .withHeader("Content-Type", "application/json");
    }

    @Override
    public ApiRequest updatePlaceholders(ConfigProperties configProperties) {
        // Call parent implementation to handle common fields
        super.updatePlaceholders(configProperties);

        // No need to process specific fields as they don't contain placeholders
        return this;
    }
}
