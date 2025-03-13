package com.pingidentity.pingam.model.ldap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiRequest;
import com.pingidentity.pingam.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request for testing LDAP connectivity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestLdapConnectionRequest extends ApiRequest {

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("bindDN")
    private String bindDN;

    @JsonProperty("bindPassword")
    private String bindPassword;

    @JsonProperty("ssl")
    private Boolean ssl;

    /**
     * Create a default LDAP connection test request
     */
    public static TestLdapConnectionRequest createDefault() {
        return (TestLdapConnectionRequest) TestLdapConnectionRequest.builder()
                .endpoint("/json/realms/root/realm-config/services/id-repositories/LDAPv3ForOpenAM/test-connection")
                .method(HttpMethod.POST)
                .host("${ldap.host}")
                .port(389)
                .bindDN("${ldap.bindDN}")
                .bindPassword("${ldap.password}")
                .ssl(false)
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}");
    }
}
