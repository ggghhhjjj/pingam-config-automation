package com.pingidentity.pingam.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.model.ApiRequest;
import com.pingidentity.pingam.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request for authenticating with PingAm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationRequest extends ApiRequest {

    @JsonIgnore
    private String username;

    @JsonIgnore
    private String password;

    /**
     * Initializes a new authentication request with default values
     * @return A new authentication request
     */
    public static AuthenticationRequest createDefault(ConfigProperties configProperties) {
        return (AuthenticationRequest) AuthenticationRequest.builder()
                .endpoint("/json/authenticate")
                .method(HttpMethod.POST)
                .build()
                .withHeader("X-OpenAM-Username", configProperties.getProperty("api.username"))
                .withHeader("X-OpenAM-Password", configProperties.getProperty("api.password"));
    }

    @Override
    @JsonIgnore
    public Object getBody() {
        // Authentication request has an empty body since credentials go in headers
        return null;
    }
}
