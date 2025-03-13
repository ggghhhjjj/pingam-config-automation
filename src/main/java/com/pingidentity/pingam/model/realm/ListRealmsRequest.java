package com.pingidentity.pingam.model.realm;

import com.pingidentity.pingam.model.ApiRequest;
import com.pingidentity.pingam.model.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request for listing realms
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ListRealmsRequest extends ApiRequest {

    /**
     * Create a default realms listing request
     */
    public static ListRealmsRequest createDefault() {
        return (ListRealmsRequest) ListRealmsRequest.builder().endpoint("/json/realms").method(HttpMethod.GET).build().withHeader(
                "iPlanetDirectoryPro", "${auth.token}");
    }

    @Override
    public Object getBody() {
        return null;
    }
}
