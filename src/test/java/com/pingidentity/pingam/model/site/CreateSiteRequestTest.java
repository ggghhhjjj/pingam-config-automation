package com.pingidentity.pingam.model.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CreateSiteRequestTest {

    @Test
    void testSetSecondaryURLsFromProperty() {
        // Setup
        CreateSiteRequest request = CreateSiteRequest.createDefault();

        // Test with single URL
        request.setSecondaryURLsFromProperty("http://example.com");
        assertEquals(1, request.getSecondaryURLs().size());
        assertEquals("http://example.com", request.getSecondaryURLs().get(0));

        // Test with multiple URLs
        request.setSecondaryURLsFromProperty("http://example1.com,http://example2.com,http://example3.com");
        assertEquals(3, request.getSecondaryURLs().size());
        assertEquals("http://example1.com", request.getSecondaryURLs().get(0));
        assertEquals("http://example2.com", request.getSecondaryURLs().get(1));
        assertEquals("http://example3.com", request.getSecondaryURLs().get(2));

        // Test with empty string
        request.setSecondaryURLsFromProperty("");
        // Should not change the previous value
        assertEquals(3, request.getSecondaryURLs().size());

        // Test with null
        request.setSecondaryURLsFromProperty(null);
        // Should not change the previous value
        assertEquals(3, request.getSecondaryURLs().size());

        // Test with unparsed property placeholder
        request.setSecondaryURLsFromProperty("${site.secondaryUrls}");
        // Should not change the previous value
        assertEquals(3, request.getSecondaryURLs().size());
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