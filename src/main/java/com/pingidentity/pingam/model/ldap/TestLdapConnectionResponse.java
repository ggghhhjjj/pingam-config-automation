package com.pingidentity.pingam.model.ldap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response from LDAP connection test
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestLdapConnectionResponse extends ApiResponse {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("detail")
    private Map<String, Object> detail;

    @Override
    public boolean isValid() {
        return code != null && code == 200;
    }
}