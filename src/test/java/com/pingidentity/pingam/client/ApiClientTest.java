package com.pingidentity.pingam.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.exception.ApiException;
import com.pingidentity.pingam.model.HttpMethod;
import com.pingidentity.pingam.model.auth.AuthenticationRequest;
import com.pingidentity.pingam.model.auth.AuthenticationResponse;
import com.pingidentity.pingam.model.site.CreateSiteRequest;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiClientTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity httpEntity;

    @Mock
    private ConfigProperties configProperties;

    private ApiClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Common setup for mocks
        when(configProperties.getProperty("api.baseUrl")).thenReturn("http://test-server:8080/sso");
        when(configProperties.getProperty("api.version", "resource=2.0,protocol=1.0")).thenReturn("resource=2.0," +
                "protocol=1.0");

        objectMapper = new ObjectMapper();
        apiClient = new ApiClient(configProperties, objectMapper, httpClient);
    }

    @Test
    void testExecuteAuthenticationRequest() throws IOException, ApiException {
        // Setup
        AuthenticationRequest request = (AuthenticationRequest) AuthenticationRequest.builder().endpoint("/json" +
                "/authenticate").method(HttpMethod.POST).build().withHeader("X-OpenAM-Username", "testUser").withHeader("X-OpenAM-Password", "testPassword");

        String responseJson = "{\"tokenId\":\"test-token\",\"successUrl\":\"/sso/console\",\"realm\":\"/\"}";

        // Mock response setup
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        // Capture the entity content as a string
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseJson.getBytes()));

        // Test
        AuthenticationResponse response = apiClient.execute(request, AuthenticationResponse.class);

        // Verify
        assertNotNull(response);
        assertEquals("test-token", response.getTokenId());
        assertEquals("/sso/console", response.getSuccessUrl());
        assertEquals("/", response.getRealm());

        // Verify HTTP request
        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(requestCaptor.capture());

        HttpUriRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.getMethod());
        assertTrue(capturedRequest.getURI().toString().endsWith("/json/authenticate"));
        assertEquals("application/json", capturedRequest.getFirstHeader("Content-Type").getValue());
        assertEquals("testUser", capturedRequest.getFirstHeader("X-OpenAM-Username").getValue());
        assertEquals("testPassword", capturedRequest.getFirstHeader("X-OpenAM-Password").getValue());
    }

    @Test
    void testResolvePropertiesInRequestBody() throws IOException, ApiException {
        // Setup
        CreateSiteRequest request = (CreateSiteRequest) CreateSiteRequest.builder()
                .endpoint("/json/global-config/sites")
                .method(HttpMethod.POST)
                .id("${site.id}")
                .url("${site.url}")
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}")
                .withQueryParam("_action", "create");

        // Mock property resolution
        when(configProperties.getProperty("site.id")).thenReturn("test-site-id");
        when(configProperties.getProperty("site.url")).thenReturn("http://test-site-url.com");
        when(configProperties.getProperty("auth.token")).thenReturn("test-token");

        // Mock response setup
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

        // Execute request
        apiClient.execute(request, com.pingidentity.pingam.model.site.CreateSiteResponse.class);

        // Capture the HTTP request to verify body
        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(requestCaptor.capture());

        HttpUriRequest capturedRequest = requestCaptor.getValue();
        assertTrue(capturedRequest instanceof HttpPost);

        // Extract and verify request body
        HttpPost postRequest = (HttpPost) capturedRequest;
        HttpEntity entity = postRequest.getEntity();
        String requestBody = EntityUtils.toString(entity);

        // Verify property placeholders were resolved in the body
        assertTrue(requestBody.contains("\"_id\":\"test-site-id\""));
        assertTrue(requestBody.contains("\"url\":\"http://test-site-url.com\""));
        // Could add other assertions as needed

        // Verify headers were also resolved
        assertEquals("test-token", capturedRequest.getFirstHeader("iPlanetDirectoryPro").getValue());
    }

    @Test
    void testExecuteWithErrorResponse() throws IOException {
        // Setup
        AuthenticationRequest request =
                AuthenticationRequest.builder().endpoint("/json/authenticate").method(HttpMethod.POST).build();

        String errorJson = "{\"code\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid credentials\"}";

        // Mock response setup
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(401);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        // Capture the entity content as a string
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(errorJson.getBytes()));

        // Test and verify exception
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiClient.execute(request, AuthenticationResponse.class);
        });

        assertTrue(exception.getMessage().contains("401"));
    }

    @Test
    void testPropertyPlaceholderResolution() throws IOException, ApiException {
        // Setup
        AuthenticationRequest request = (AuthenticationRequest) AuthenticationRequest.builder().endpoint("/json" +
                "/authenticate").method(HttpMethod.POST).build().withHeader("X-OpenAM-Username", "${api.username}").withHeader("X-OpenAM-Password", "${api.password}");

        String responseJson = "{\"tokenId\":\"test-token\",\"successUrl\":\"/sso/console\",\"realm\":\"/\"}";

        // Mock property resolution
        when(configProperties.getProperty("api.username")).thenReturn("resolvedUser");
        when(configProperties.getProperty("api.password")).thenReturn("resolvedPassword");

        // Mock response setup
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        // Capture the entity content as a string
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(responseJson.getBytes()));

        // Test
        apiClient.execute(request, AuthenticationResponse.class);

        // Verify headers were resolved
        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(requestCaptor.capture());

        HttpUriRequest capturedRequest = requestCaptor.getValue();
        assertEquals("resolvedUser", capturedRequest.getFirstHeader("X-OpenAM-Username").getValue());
        assertEquals("resolvedPassword", capturedRequest.getFirstHeader("X-OpenAM-Password").getValue());
    }
}
