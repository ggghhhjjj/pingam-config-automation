package identity.am.automation.model.server;

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

/**
 * Request for cloning a server
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloneServerRequest extends ApiRequest {

    @JsonProperty("clonedUrl")
    private String clonedUrl;

    /**
     * Create a default request for cloning a server
     */
    public static CloneServerRequest createDefault() {
        return (CloneServerRequest) CloneServerRequest.builder()
                .endpoint("/json/global-config/servers/${server.id}")
                .method(HttpMethod.POST)
                .clonedUrl("${server.clone.url}")
                .build()
                .withHeader("${api.auth.cookie.name}", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0")
                .withQueryParam("_action", "clone");
    }

    @Override
    public ApiRequest updatePlaceholders(ConfigProperties configProperties) {
        // Call parent implementation to handle common fields
        super.updatePlaceholders(configProperties);

        // Update clone-specific fields
        if (clonedUrl != null && clonedUrl.contains("${")) {
            clonedUrl = resolvePlaceholders(clonedUrl, configProperties);
        }

        return this;
    }
}