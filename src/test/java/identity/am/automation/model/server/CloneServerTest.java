package identity.am.automation.model.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import identity.am.automation.config.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloneServerTest {

    @Mock
    private ConfigProperties configProperties;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Set up common mocks in lenient mode
        lenient().when(configProperties.getProperty("server.id")).thenReturn("01");
        lenient().when(configProperties.getProperty("auth.token")).thenReturn("test-token");
        lenient().when(configProperties.getProperty("api.auth.cookie.name")).thenReturn("iPlanetDirectoryPro");
                lenient().when(configProperties.getProperty("server.clone.url")).thenReturn("http://web2.local:8080/sso");
    }

    @Test
    void testCloneServerRequest() {
        // Create the request
        CloneServerRequest request = CloneServerRequest.createDefault();

        // Verify the request properties
        assertEquals("/json/global-config/servers/${server.id}", request.getEndpoint());
        assertEquals("${auth.token}", request.getHeaders().get("${api.auth.cookie.name}"));
        assertEquals("protocol=1.0,resource=1.0", request.getHeaders().get("Accept-API-Version"));
        assertEquals("clone", request.getQueryParams().get("_action"));
        assertEquals("${server.clone.url}", request.getClonedUrl());

        // Update placeholders
        request.updatePlaceholders(configProperties);

        // Verify placeholders were resolved
        assertEquals("/json/global-config/servers/01", request.getEndpoint());
        assertEquals("test-token", request.getHeaders().get("iPlanetDirectoryPro"));
        assertEquals("http://web2.local:8080/sso", request.getClonedUrl());
    }

    @Test
    void testCloneServerResponse() throws Exception {
        // Sample JSON response
        String jsonResponse = "{\"clonedId\":\"05\",\"clonedUrl\":\"http://web2.local:8080/sso\"}";

        // Deserialize JSON
        CloneServerResponse response = objectMapper.readValue(jsonResponse, CloneServerResponse.class);

        // Verify response properties
        assertEquals("05", response.getClonedId());
        assertEquals("http://web2.local:8080/sso", response.getClonedUrl());
        assertTrue(response.isValid());
    }

    @Test
    void testInvalidCloneServerResponse() {
        // Create an invalid response
        CloneServerResponse response = new CloneServerResponse();
        assertFalse(response.isValid());

        // Test with only ID
        response.setClonedId("05");
        assertFalse(response.isValid());

        // Test with only URL
        response = new CloneServerResponse();
        response.setClonedUrl("http://web2.local:8080/sso");
        assertFalse(response.isValid());
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Create a request
        CloneServerRequest request = new CloneServerRequest();
        request.setClonedUrl("http://web2.local:8080/sso");

        // Convert to JSON
        String json = objectMapper.writeValueAsString(request);

        // Verify
        assertTrue(json.contains("\"clonedUrl\":\"http://web2.local:8080/sso\""));
    }
}