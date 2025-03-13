package com.pingidentity.pingam.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPropertiesTest {

    private ConfigProperties configProperties;
    
    @TempDir
    Path tempDir;
    
    private File propertiesFile;

    @BeforeEach
    void setUp() throws IOException {
        configProperties = new ConfigProperties();
        
        // Create a temporary properties file for testing
        propertiesFile = tempDir.resolve("test.properties").toFile();
        
        Properties props = new Properties();
        props.setProperty("api.baseUrl", "http://test-server:8080/sso");
        props.setProperty("api.username", "testUser");
        props.setProperty("api.password", "testPassword");
        props.setProperty("realm.name", "testRealm");
        
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            props.store(writer, "Test Properties");
        }
    }

    @Test
    void testLoadFromFile() throws IOException {
        // Test
        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
        
        // Verify
        assertEquals("http://test-server:8080/sso", configProperties.getProperty("api.baseUrl"));
        assertEquals("testUser", configProperties.getProperty("api.username"));
        assertEquals("testPassword", configProperties.getProperty("api.password"));
        assertEquals("testRealm", configProperties.getProperty("realm.name"));
    }

    @Test
    void testGetPropertyWithDefault() throws IOException {
        // Setup
        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
        
        // Test and verify
        assertEquals("testUser", configProperties.getProperty("api.username"));
        assertEquals("defaultValue", configProperties.getProperty("non.existent.property", "defaultValue"));
    }

    @Test
    void testRuntimePropertyOverride() throws IOException {
        // Setup
        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
        
        // Test
        configProperties.setRuntimeProperty("api.username", "runtimeUser");
        
        // Verify
        assertEquals("runtimeUser", configProperties.getProperty("api.username"));
        assertEquals("testPassword", configProperties.getProperty("api.password")); // Unchanged
    }

    @Test
    void testClearRuntimeProperties() throws IOException {
        // Setup
        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
        configProperties.setRuntimeProperty("api.username", "runtimeUser");
        configProperties.setRuntimeProperty("new.property", "newValue");
        
        // Test
        configProperties.clearRuntimeProperties();
        
        // Verify
        assertEquals("testUser", configProperties.getProperty("api.username")); // Back to file value
        assertNull(configProperties.getProperty("new.property")); // No longer exists
    }

    @Test
    void testGetAllProperties() throws IOException {
        // Setup
        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
        configProperties.setRuntimeProperty("api.username", "runtimeUser");
        configProperties.setRuntimeProperty("new.property", "newValue");
        
        // Test
        Map<String, String> allProps = configProperties.getAllProperties();
        
        // Verify
        assertEquals("runtimeUser", allProps.get("api.username")); // Runtime value
        assertEquals("testPassword", allProps.get("api.password")); // File value
        assertEquals("newValue", allProps.get("new.property")); // Runtime only value
    }

    @Test
    void testFileNotFound() {
        // Test and verify
        IOException exception = assertThrows(IOException.class, () -> {
            configProperties.loadFromFile("non_existent_file.properties");
        });
        
        assertTrue(exception.getMessage().contains("non_existent_file.properties"));
    }
}