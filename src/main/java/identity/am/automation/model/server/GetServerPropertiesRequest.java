package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Request for getting server properties
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetServerPropertiesRequest extends ApiRequest {

    /**
     * Create a default request for getting properties for a specific server
     */
    public static GetServerPropertiesRequest createDefault() {
        return (GetServerPropertiesRequest) GetServerPropertiesRequest.builder()
                .endpoint("/json/global-config/servers/${server.id}/properties/general")
                .method(HttpMethod.GET)
                .build()
                .withHeader("${api.auth.cookie.name}", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0");
    }
}
