package identity.am.automation.model.realm;

import com.fasterxml.jackson.annotation.JsonInclude;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Request for getting all realms
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRealmsRequest extends ApiRequest {

    /**
     * Create a default request for getting all realms
     */
    public static GetRealmsRequest createDefault() {
        return (GetRealmsRequest) GetRealmsRequest.builder()
                .endpoint("/json/global-config/realms")
                .method(HttpMethod.GET)
                .build()
                .withHeader("${api.auth.cookie.name}", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=2.0,resource=1.0")
                .withQueryParam("_queryFilter", "true");
    }
}
