package identity.am.automation.model.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import identity.am.automation.config.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerAdvancedPropertiesTest {

    @Mock
    private ConfigProperties configProperties;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Default return for any other property
        lenient().when(configProperties.getProperty(anyString())).thenReturn(null);

        // Set up common mocks in lenient mode
        lenient().when(configProperties.getProperty("server.id")).thenReturn("server-1");
        lenient().when(configProperties.getProperty("auth.token")).thenReturn("test-token");
        lenient().when(configProperties.getProperty("api.auth.cookie.name")).thenReturn("iPlanetDirectoryPro");
        lenient().when(configProperties.getProperty("server.lbcookie.value", "web1")).thenReturn("web1");
        lenient().when(configProperties.getProperty("server.replication.port", "58989")).thenReturn("58989");
    }

    @Test
    void testGetServerAdvancedPropertiesRequest() {
        // Create the request
        GetServerAdvancedPropertiesRequest request = GetServerAdvancedPropertiesRequest.createDefault();

        // Verify the request properties
        assertEquals("/json/global-config/servers/${server.id}/properties/advanced", request.getEndpoint());
        assertEquals("${auth.token}", request.getHeaders().get("${api.auth.cookie.name}"));
        assertEquals("protocol=1.0,resource=1.0", request.getHeaders().get("Accept-API-Version"));

        // Update placeholders
        request.updatePlaceholders(configProperties);

        // Verify placeholders were resolved
        assertEquals("/json/global-config/servers/server-1/properties/advanced", request.getEndpoint());
        assertEquals("test-token", request.getHeaders().get("iPlanetDirectoryPro"));
    }

    @Test
    void testUpdateServerAdvancedPropertiesRequest() throws Exception {
        // Create sample original properties
        Map<String, Object> originalProperties = new HashMap<>();
        originalProperties.put("com.iplanet.am.lbcookie.value", "old-value");
        originalProperties.put("com.sun.embedded.replicationport", "");
        originalProperties.put("com.sun.identity.common.systemtimerpool.size", "3");
        originalProperties.put("com.sun.identity.cookie.httponly", true);

        // Convert to JSON
        String originalPropertiesJson = objectMapper.writeValueAsString(originalProperties);
        when(configProperties.getProperty("server.advanced.properties.json")).thenReturn(originalPropertiesJson);

        // Create the request
        UpdateServerAdvancedPropertiesRequest request = UpdateServerAdvancedPropertiesRequest.createDefault();

        // Update placeholders
        request.updatePlaceholders(configProperties);

        // Verify the request has the updated properties
        Map<String, Object> properties = request.getProperties();
        assertEquals("web1", properties.get("com.iplanet.am.lbcookie.value"));
        assertEquals("58989", properties.get("com.sun.embedded.replicationport"));
        assertEquals("3", properties.get("com.sun.identity.common.systemtimerpool.size"));
        assertEquals(true, properties.get("com.sun.identity.cookie.httponly"));
    }

    @Test
    void testUpdateServerAdvancedPropertiesWithInvalidJson() {
        // Use invalid JSON
        when(configProperties.getProperty("server.advanced.properties.json")).thenReturn("invalid-json");

        // Create and update request
        UpdateServerAdvancedPropertiesRequest request = UpdateServerAdvancedPropertiesRequest.createDefault();
        request.updatePlaceholders(configProperties);

        // Verify fallback behavior
        Map<String, Object> properties = request.getProperties();
        assertEquals(2, properties.size());
        assertEquals("web1", properties.get("com.iplanet.am.lbcookie.value"));
        assertEquals("58989", properties.get("com.sun.embedded.replicationport"));
    }

    @Test
    void testUpdateServerAdvancedPropertiesWithNoJson() {
        // No JSON available
        when(configProperties.getProperty("server.advanced.properties.json")).thenReturn(null);

        // Create and update request
        UpdateServerAdvancedPropertiesRequest request = UpdateServerAdvancedPropertiesRequest.createDefault();
        request.updatePlaceholders(configProperties);

        // Verify default values
        Map<String, Object> properties = request.getProperties();
        assertEquals(2, properties.size());
        assertEquals("web1", properties.get("com.iplanet.am.lbcookie.value"));
        assertEquals("58989", properties.get("com.sun.embedded.replicationport"));
    }

    @Test
    void testResponseAccessors() {
        // Create a response
        GetServerAdvancedPropertiesResponse response = new GetServerAdvancedPropertiesResponse();

        // Add some properties
        response.setProperty("com.iplanet.am.lbcookie.value", "web1");
        response.setProperty("com.sun.embedded.replicationport", "58989");
        response.setProperty("com.sun.identity.cookie.httponly", true);

        // Test getters
        assertEquals("web1", response.getLbCookieValue());
        assertEquals("58989", response.getReplicationPort());
        assertTrue(response.isValid());

        // Test empty
        GetServerAdvancedPropertiesResponse emptyResponse = new GetServerAdvancedPropertiesResponse();
        assertNull(emptyResponse.getLbCookieValue());
        assertNull(emptyResponse.getReplicationPort());
        assertFalse(emptyResponse.isValid());
    }
}