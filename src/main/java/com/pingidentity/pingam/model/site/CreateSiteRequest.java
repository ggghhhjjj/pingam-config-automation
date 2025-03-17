package com.pingidentity.pingam.model.site;


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
 * Request for creating a PingAm site
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateSiteRequest extends ApiRequest {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("secondaryURLs")
    private List<String> secondaryURLs;

    /**
     * Create a default site creation request
     */
    public static CreateSiteRequest createDefault() {
        // Create a default request with base properties
        CreateSiteRequest request = (CreateSiteRequest) CreateSiteRequest.builder()
                .endpoint("/json/global-config/sites")
                .method(HttpMethod.POST)
                .id("${site.id}")
                .url("${site.url}")
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}")
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0")
                .withQueryParam("_action", "create");

        // Handle secondary URLs separately to transform from a comma-separated property
        // to a list for the JSON request
        request.setSecondaryURLsFromProperty("${site.secondaryUrls}");

        return request;
    }

    /**
     * Sets secondary URLs from a comma-separated property string
     * @param propertyValue A comma-separated list of URLs
     */
    public void setSecondaryURLsFromProperty(String propertyValue) {
        if (propertyValue != null && !propertyValue.isEmpty() && !propertyValue.equals("${site.secondaryUrls}")) {
            // Split by comma and create a list
            String[] urls = propertyValue.split(",");
            this.secondaryURLs = java.util.Arrays.asList(urls);
        }
    }
}
