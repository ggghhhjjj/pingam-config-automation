package com.pingidentity.pingam.model.realm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiRequest;
import com.pingidentity.pingam.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Request for creating a new realm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRealmRequest extends ApiRequest {

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("name")
    private String name;

    @JsonProperty("aliases")
    private List<String> aliases;

    /**
     * Create a default realm creation request with the given name
     */
    public static CreateRealmRequest createDefault(String realmName) {
        return (CreateRealmRequest) CreateRealmRequest.builder().endpoint("/json/realms").method(HttpMethod.POST).active(true).parentPath("/").name(realmName).build().withHeader("iPlanetDirectoryPro", "${auth.token}");
    }
}
