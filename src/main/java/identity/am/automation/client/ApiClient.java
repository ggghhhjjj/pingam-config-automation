package identity.am.automation.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.exception.ApiException;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Generic API client for PingAm REST API interactions
 */
@Slf4j
public class ApiClient {
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    // Cache configuration values that don't change
    private final String baseUrl;
    private final String apiVersion;

    public ApiClient(ConfigProperties configProperties) {
        this(configProperties, new ObjectMapper(), HttpClients.createDefault());
    }

    public ApiClient(ConfigProperties configProperties, ObjectMapper objectMapper, CloseableHttpClient httpClient) {
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;

        // Initialize and cache configuration values once
        this.baseUrl = configProperties.resolveBaseUrl();
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalArgumentException("api.baseUrl is required in configuration");
        }

        this.apiVersion = configProperties.getProperty("api.version", "resource=2.0,protocol=1.0");
        log.debug("Initialized API client with baseUrl: {}, apiVersion: {}", baseUrl, apiVersion);
    }

    /**
     * Execute an API request and return the response
     *
     * @param request The API request to execute
     * @param responseClass The class to deserialize the response into
     * @param <T> The type of response
     * @return The deserialized response
     * @throws ApiException If there is an error executing the request
     */
    public <T extends ApiResponse> T execute(ApiRequest request, Class<T> responseClass) throws ApiException {
        try {
            // Update placeholders in the request before creating the HTTP request
            request.updatePlaceholders(configProperties);

            HttpUriRequest httpRequest = createHttpRequest(request);

            // Add common headers
            httpRequest.addHeader("Content-Type", "application/json");
            httpRequest.addHeader("Accept-API-Version", apiVersion);

            // Add request-specific headers
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                httpRequest.addHeader(entry.getKey(), entry.getValue());
            }

            log.debug("Executing request: {} {}", httpRequest.getMethod(), httpRequest.getURI());

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity) : "";

                log.debug("Received response with status code: {}", statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    return deserializeResponse(responseBody, responseClass);
                } else {
                    throw new ApiException("API request failed with status: " + statusCode + ", response: " + responseBody);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new ApiException("Error executing API request", e);
        }
    }

    private HttpUriRequest createHttpRequest(ApiRequest request) throws URISyntaxException, JsonProcessingException {
        String endpoint = request.getEndpoint();
        URIBuilder uriBuilder = new URIBuilder(baseUrl + endpoint);

        // Add query parameters
        for (Map.Entry<String, String> entry : request.getQueryParams().entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        URI uri = uriBuilder.build();

        switch (request.getMethod()) {
            case GET:
                return new HttpGet(uri);
            case POST:
                HttpPost post = new HttpPost(uri);
                if (request.getBody() != null) {
                    String jsonBody = objectMapper.writeValueAsString(request.getBody());
                    post.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                }
                return post;
            case PUT:
                HttpPut put = new HttpPut(uri);
                if (request.getBody() != null) {
                    String jsonBody = objectMapper.writeValueAsString(request.getBody());
                    put.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                }
                return put;
            case DELETE:
                return new HttpDelete(uri);
            case PATCH:
                HttpPatch patch = new HttpPatch(uri);
                if (request.getBody() != null) {
                    String jsonBody = objectMapper.writeValueAsString(request.getBody());
                    patch.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                }
                return patch;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
        }
    }

    private <T extends ApiResponse> T deserializeResponse(String responseBody, Class<T> responseClass) throws ApiException {
        try {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (IOException e) {
            throw new ApiException("Error deserializing response", e);
        }
    }
}