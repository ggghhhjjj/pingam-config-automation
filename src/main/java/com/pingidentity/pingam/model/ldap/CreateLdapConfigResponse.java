package com.pingidentity.pingam.model.ldap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response from LDAP configuration creation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateLdapConfigResponse extends ApiResponse {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String revision;

    @JsonProperty("name")
    private String name;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("authenticationEnabled")
    private Boolean authenticationEnabled;

    @JsonProperty("sslEnabled")
    private Boolean sslEnabled;

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty();
    }
}