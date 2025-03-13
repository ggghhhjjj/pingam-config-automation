package com.pingidentity.pingam.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response from PingAm authentication
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse extends ApiResponse {

    @JsonProperty("tokenId")
    private String tokenId;

    @JsonProperty("successUrl")
    private String successUrl;

    @JsonProperty("realm")
    private String realm;

    @Override
    public boolean isValid() {
        return tokenId != null && !tokenId.isEmpty();
    }
}
