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

import java.util.List;

/**
 * Request for creating an LDAP configuration
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateLdapConfigRequest extends ApiRequest {

    @JsonProperty("_id")
    private String id;

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

    @JsonProperty("bindDN")
    private String bindDN;

    @JsonProperty("bindPassword")
    private String bindPassword;

    @JsonProperty("heartbeatInterval")
    private Integer heartbeatInterval;

    @JsonProperty("heartbeatTimeUnit")
    private String heartbeatTimeUnit;

    @JsonProperty("rootSuffix")
    private List<String> rootSuffix;

    @JsonProperty("minConnectionPool")
    private Integer minConnectionPool;

    @JsonProperty("maxConnectionPool")
    private Integer maxConnectionPool;

    /**
     * Create a default LDAP configuration request
     */
    public static CreateLdapConfigRequest createDefault() {
        return (CreateLdapConfigRequest) CreateLdapConfigRequest.builder().endpoint("/json/realms/root/realm-config" +
                "/services/id-repositories/LDAPv3ForOpenAM/instances/LDAP-1").method(HttpMethod.PUT).id("LDAP-1").name("LDAP-1").host("${ldap.host}").port(389).authenticationEnabled(true).sslEnabled(false).bindDN("${ldap.bindDN}").bindPassword("${ldap.password}").heartbeatInterval(10).heartbeatTimeUnit("SECONDS").minConnectionPool(1).maxConnectionPool(10).build().withHeader("iPlanetDirectoryPro", "${auth.token}").withHeader("If-None-Match", "*");
    }
}