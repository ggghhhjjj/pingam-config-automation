package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Request for getting server advanced properties
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetServerAdvancedPropertiesRequest extends ApiRequest {

    /**
     * Create a default request for getting server advanced properties
     */
    public static GetServerAdvancedPropertiesRequest createDefault() {
        return (GetServerAdvancedPropertiesRequest) GetServerAdvancedPropertiesRequest.builder()
                .endpoint("/json/global-config/servers/${server.id}/properties/advanced")
                .method(HttpMethod.GET)
                .build()
                .withHeader("${api.auth.cookie.name}", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0");
    }
}
