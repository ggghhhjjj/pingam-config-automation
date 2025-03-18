package com.pingidentity.pingam;

import com.pingidentity.pingam.config.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

class ConfigAutomationApplicationTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        ConfigProperties configProperties = new ConfigProperties();

        // Create a temporary properties file for testing
        File propertiesFile = tempDir.resolve("test.properties").toFile();

        Properties props = new Properties();
        props.setProperty("api.baseUrl", "http://test-server:8080/sso");
        props.setProperty("api.username", "testUser");
        props.setProperty("api.password", "testPassword");
        props.setProperty("site.url", "http://pco.local:18080/sso");

        try (FileWriter writer = new FileWriter(propertiesFile)) {
            props.store(writer, "Test Properties");
        }

        configProperties.loadFromFile(propertiesFile.getAbsolutePath());
    }
}
