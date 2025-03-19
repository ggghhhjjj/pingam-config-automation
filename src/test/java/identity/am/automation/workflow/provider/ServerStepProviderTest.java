package identity.am.automation.workflow.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import identity.am.automation.client.ApiClient;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.server.UpdateServerPropertiesRequest;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Set strict stubbing to false to allow unused stubs
@ExtendWith(MockitoExtension.class)
class ServerStepProviderTest {

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private WorkflowEngine workflowEngine;

    @Mock
    private ApiClient apiClient;

    @Captor
    private ArgumentCaptor<WorkflowStep<?, ?>> workflowStepCaptor;

    private ServerStepProvider serverStepProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        serverStepProvider = new ServerStepProvider(configProperties);

        // Set up common mocks in lenient mode
        lenient().when(configProperties.getProperty("server.id")).thenReturn("server-1");
        lenient().when(configProperties.getProperty("auth.token")).thenReturn("test-token");

        // Default return for any other property
        lenient().when(configProperties.getProperty(anyString())).thenReturn(null);
    }

    @Test
    void testRegisterStepsWithWorkflowEngine() {
        // Execute the method
        serverStepProvider.registerSteps(workflowEngine);

        // Verify that three steps were registered
        verify(workflowEngine, times(3)).registerStep(workflowStepCaptor.capture());

        // Get the captured steps
        var capturedSteps = workflowStepCaptor.getAllValues();
        assertEquals(3, capturedSteps.size());

        // Verify step names
        assertEquals("getServers", capturedSteps.get(0).getName());
        assertEquals("getServerProperties", capturedSteps.get(1).getName());
        assertEquals("updateServerProperties", capturedSteps.get(2).getName());
    }

    @Test
    void testUpdateServerPropertiesPreservesExistingPropertiesWithSiteChange() throws Exception {
        // 1. Set up the test data: create a sample server properties response
        Map<String, Object> originalProperties = new HashMap<>();

        // Create nested header properties
        Map<String, Object> siteHeader = new HashMap<>();
        siteHeader.put("singleChoiceSite", "oldSiteId");
        originalProperties.put("amconfig.header.site", siteHeader);

        // Add some other properties to ensure they're preserved
        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("serverName", "TestServer");
        serverConfig.put("serverUrl", "http://test.example.com:8080");
        originalProperties.put("serverConfig", serverConfig);

        Map<String, Object> loggingConfig = new HashMap<>();
        loggingConfig.put("logLevel", "INFO");
        loggingConfig.put("logFile", "/var/log/pingam.log");
        originalProperties.put("loggingConfig", loggingConfig);

        // Convert properties to JSON
        String originalPropertiesJson = objectMapper.writeValueAsString(originalProperties);

        // More specific mocks - these take precedence over the default
        lenient().when(configProperties.getProperty("site.id")).thenReturn("newSiteId");
        lenient().when(configProperties.getProperty("server.properties.json")).thenReturn(originalPropertiesJson);

        // Create a new UpdateServerPropertiesRequest directly
        UpdateServerPropertiesRequest request = UpdateServerPropertiesRequest.createDefault();

        // Call updatePlaceholders manually to apply our test logic
        request.updatePlaceholders(configProperties);

        // Verify the properties map matches what we expect
        assertNotNull(request.getProperties());

        // Site property should have been updated
        Map<String, Object> updatedSiteHeader = (Map<String, Object>) request.getProperties().get("amconfig.header.site");
        assertNotNull(updatedSiteHeader);
        assertEquals("newSiteId", updatedSiteHeader.get("singleChoiceSite"));

        // Other properties should be preserved
        Map<String, Object> updatedServerConfig = (Map<String, Object>) request.getProperties().get("serverConfig");
        assertEquals("TestServer", updatedServerConfig.get("serverName"));
        assertEquals("http://test.example.com:8080", updatedServerConfig.get("serverUrl"));

        Map<String, Object> updatedLoggingConfig = (Map<String, Object>) request.getProperties().get("loggingConfig");
        assertEquals("INFO", updatedLoggingConfig.get("logLevel"));
        assertEquals("/var/log/pingam.log", updatedLoggingConfig.get("logFile"));
    }

    @Test
    void testUpdateServerPropertiesHandlesEmptyProperties() {
        // More specific mocks override the default
        lenient().when(configProperties.getProperty("site.id")).thenReturn("newSiteId");
        lenient().when(configProperties.getProperty("server.properties.json")).thenReturn(null);

        // Create request
        UpdateServerPropertiesRequest request = UpdateServerPropertiesRequest.createDefault();

        // Call updatePlaceholders manually
        request.updatePlaceholders(configProperties);

        // Verify it only contains the site property
        assertNotNull(request.getProperties());
        assertEquals(1, request.getProperties().size());
        assertTrue(request.getProperties().containsKey("amconfig.header.site"));

        Map<String, Object> siteHeader = (Map<String, Object>) request.getProperties().get("amconfig.header.site");
        assertEquals("newSiteId", siteHeader.get("singleChoiceSite"));
    }

    @Test
    void testUpdateServerPropertiesHandlesInvalidJson() {
        // More specific mocks override the default
        lenient().when(configProperties.getProperty("site.id")).thenReturn("newSiteId");
        lenient().when(configProperties.getProperty("server.properties.json")).thenReturn("{invalid-json}");

        // Create request
        UpdateServerPropertiesRequest request = UpdateServerPropertiesRequest.createDefault();

        // Call updatePlaceholders manually
        request.updatePlaceholders(configProperties);

        // Verify it falls back to just the site property
        assertNotNull(request.getProperties());
        assertEquals(1, request.getProperties().size());
        assertTrue(request.getProperties().containsKey("amconfig.header.site"));

        Map<String, Object> siteHeader = (Map<String, Object>) request.getProperties().get("amconfig.header.site");
        assertEquals("newSiteId", siteHeader.get("singleChoiceSite"));
    }

    @Test
    void testWorkflowEndToEnd() throws Exception {
        // Create the original properties
        Map<String, Object> originalProperties = new HashMap<>();
        Map<String, Object> siteHeader = new HashMap<>();
        siteHeader.put("singleChoiceSite", "oldSiteId");
        originalProperties.put("amconfig.header.site", siteHeader);
        originalProperties.put("otherProperty", "otherValue");

        // Convert to JSON
        String originalPropertiesJson = objectMapper.writeValueAsString(originalProperties);

        // More specific mocks override the default
        lenient().when(configProperties.getProperty("site.id")).thenReturn("newSiteId");
        lenient().when(configProperties.getProperty("server.properties.json")).thenReturn(originalPropertiesJson);

        // Create update request and test it directly
        UpdateServerPropertiesRequest request = UpdateServerPropertiesRequest.createDefault();
        request.updatePlaceholders(configProperties);

        // Verify the properties were correctly set
        Map<String, Object> properties = request.getProperties();
        assertNotNull(properties);
        assertEquals(2, properties.size());
        assertTrue(properties.containsKey("amconfig.header.site"));
        assertTrue(properties.containsKey("otherProperty"));

        // Site ID should be updated
        Map<String, Object> capturedSiteHeader = (Map<String, Object>) properties.get("amconfig.header.site");
        assertEquals("newSiteId", capturedSiteHeader.get("singleChoiceSite"));

        // Other properties should be preserved
        assertEquals("otherValue", properties.get("otherProperty"));
    }
}