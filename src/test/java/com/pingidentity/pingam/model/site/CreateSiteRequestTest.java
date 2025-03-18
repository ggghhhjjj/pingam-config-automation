package com.pingidentity.pingam.model.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingidentity.pingam.config.ConfigProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateSiteRequestTest {

    @Test
    void testSetSecondaryURLsFromProperty() {
        // Setup mock ConfigProperties
        ConfigProperties mockConfig = mock(ConfigProperties.class);
        when(mockConfig.getProperty("site.id")).thenReturn("testSiteId");
        when(mockConfig.getProperty("site.url")).thenReturn("http://test.example.com");
        when(mockConfig.getProperty("auth.token")).thenReturn("test-token");

        // Test with single URL
        when(mockConfig.getProperty("site.secondaryUrls")).thenReturn("http://example.com");
        CreateSiteRequest request = CreateSiteRequest.createDefault(mockConfig);

        assertEquals(1, request.getSecondaryURLs().size());
        assertEquals("http://example.com", request.getSecondaryURLs().get(0));

        // Test with multiple URLs
        when(mockConfig.getProperty("site.secondaryUrls")).thenReturn("http://example1.com,http://example2.com,http://example3.com");
        request = CreateSiteRequest.createDefault(mockConfig);

        assertEquals(3, request.getSecondaryURLs().size());
        assertEquals("http://example1.com", request.getSecondaryURLs().get(0));
        assertEquals("http://example2.com", request.getSecondaryURLs().get(1));
        assertEquals("http://example3.com", request.getSecondaryURLs().get(2));

        // Test with empty string
        when(mockConfig.getProperty("site.secondaryUrls")).thenReturn("");
        request = CreateSiteRequest.createDefault(mockConfig);

        // Should not set secondaryURLs
        assertNull(request.getSecondaryURLs());

        // Test with null
        when(mockConfig.getProperty("site.secondaryUrls")).thenReturn(null);
        request = CreateSiteRequest.createDefault(mockConfig);

        // Should not set secondaryURLs
        assertNull(request.getSecondaryURLs());
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Setup
        ObjectMapper mapper = new ObjectMapper();
        CreateSiteRequest request = new CreateSiteRequest();
        request.setId("testSite");
        request.setUrl("http://test.example.com");
        request.setSecondaryURLs(Arrays.asList("http://alt1.example.com", "http://alt2.example.com"));

        // Convert to JSON
        String json = mapper.writeValueAsString(request);

        // Verify
        assertTrue(json.contains("\"_id\":\"testSite\""));
        assertTrue(json.contains("\"url\":\"http://test.example.com\""));
        assertTrue(json.contains("\"secondaryURLs\":["));
        assertTrue(json.contains("\"http://alt1.example.com\""));
        assertTrue(json.contains("\"http://alt2.example.com\""));
    }

    @Test
    void testHandlingOfNullSecondaryURLs() {
        // Setup
        CreateSiteRequest request = new CreateSiteRequest();
        request.setId("testSite");
        request.setUrl("http://test.example.com");

        // Initially secondaryURLs should be null
        assertNull(request.getSecondaryURLs());

        // After setting from property, should handle null gracefully
        request.setSecondaryURLsFromProperty(null);
        assertNull(request.getSecondaryURLs());

        // After setting from empty property, should still be null
        request.setSecondaryURLsFromProperty("");
        assertNull(request.getSecondaryURLs());

        // After setting valid property, should have values
        request.setSecondaryURLsFromProperty("http://example.com");
        assertNotNull(request.getSecondaryURLs());
        assertEquals(1, request.getSecondaryURLs().size());
    }
}