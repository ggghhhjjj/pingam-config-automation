package identity.am.automation.model.site;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
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
    public static CreateSiteRequest createDefault(ConfigProperties configProperties) {
        // Create a default request with base properties
        CreateSiteRequest request = (CreateSiteRequest) CreateSiteRequest.builder()
                .endpoint("/json/global-config/sites")
                .method(HttpMethod.POST)
                .id(resolveSiteId(configProperties.getProperty("site.url")))
                .url(configProperties.getProperty("site.url"))
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}") // This will be resolved at runtime,
                .withHeader("Accept-API-Version", "protocol=1.0,resource=1.0")
                .withQueryParam("_action", "create");

        // Handle secondary URLs separately to transform from a comma-separated property
        // to a list for the JSON request
        request.setSecondaryURLsFromProperty(configProperties.getProperty("site.secondaryUrls"));

        return request;
    }

    @Override
    public ApiRequest updatePlaceholders(ConfigProperties configProperties) {
        // Call parent implementation to handle common fields
        super.updatePlaceholders(configProperties);

        // Update site-specific fields
        if (id != null && id.contains("${")) {
            id = resolvePlaceholders(id, configProperties);
        }

        if (url != null && url.contains("${")) {
            url = resolvePlaceholders(url, configProperties);
        }

        // Update secondary URLs
        if (secondaryURLs != null) {
            List<String> updatedSecondaryURLs = new ArrayList<>();
            for (String secondaryURL : secondaryURLs) {
                if (secondaryURL != null && secondaryURL.contains("${")) {
                    updatedSecondaryURLs.add(resolvePlaceholders(secondaryURL, configProperties));
                } else {
                    updatedSecondaryURLs.add(secondaryURL);
                }
            }
            this.secondaryURLs = updatedSecondaryURLs;
        }

        return this;
    }

    /**
     * Sets secondary URLs from a comma-separated property string
     * @param propertyValue A comma-separated list of URLs
     */
    void setSecondaryURLsFromProperty(String propertyValue) {
        if (propertyValue != null && !propertyValue.isEmpty() && !propertyValue.equals("${site.secondaryUrls}")) {
            // Split by comma and create a list
            String[] urls = propertyValue.split(",");
            this.secondaryURLs = java.util.Arrays.asList(urls);
        }
    }

    private static String resolveSiteId(String url) {
        // Remove protocol (http://, https://)
        String result = url.replaceAll("^(http|https)://", "");

        // Remove all non-alphanumeric characters
        result = result.replaceAll("[^a-zA-Z0-9]", "");

        // Limit length if needed (optional, adjust max length as appropriate)
        final int maxLength = 30;
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }

        return result;
    }
}