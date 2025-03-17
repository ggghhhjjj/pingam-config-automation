package com.pingidentity.pingam;

import com.pingidentity.pingam.config.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigAutomationApplicationTest {

    private ConfigAutomationApplication application;
    private ConfigProperties configProperties;

    @TempDir
    Path tempDir;

    private File propertiesFile;

    @BeforeEach
    void setUp() throws IOException {
        application = new ConfigAutomationApplication();
        configProperties = new ConfigProperties();

        // Create a temporary properties file for testing
        propertiesFile = tempDir.resolve("test.properties").toFile();

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

    @Test
    void testGenerateSiteIdFromUrl() throws Exception {
        // Use reflection to access the private method
        Method generateSiteIdMethod = ConfigAutomationApplication.class.getDeclaredMethod("generateSiteIdFromUrl", String.class);
        generateSiteIdMethod.setAccessible(true);

        // Test various URLs
        assertEquals("pcolocalportpath", generateSiteIdMethod.invoke(application, "http://pco.local:port/path"));
        assertEquals("example123com", generateSiteIdMethod.invoke(application, "https://example-123.com"));
        assertEquals("subdexamplecom8080sso", generateSiteIdMethod.invoke(application, "http://sub.d.example.com:8080/sso"));
        assertEquals("192168110", generateSiteIdMethod.invoke(application, "http://192.168.1.10"));
    }

    @Test
    void testProcessRuntimeProperties() throws Exception {
        // Use reflection to access the private method
        Method processRuntimePropertiesMethod = ConfigAutomationApplication.class.getDeclaredMethod("processRuntimeProperties", ConfigProperties.class, boolean.class);
        processRuntimePropertiesMethod.setAccessible(true);

        // Case 1: site.id is not set, site.url is set
        configProperties.setRuntimeProperty("site.id", null); // Ensure site.id is not set
        processRuntimePropertiesMethod.invoke(application, configProperties, true);

        // Verify site.id was generated from URL
        assertEquals("pcolocal18080sso", configProperties.getProperty("site.id"));

        // Case 2: site.id is already set, should not override
        configProperties.setRuntimeProperty("site.id", "predefined-id");
        processRuntimePropertiesMethod.invoke(application, configProperties, true);

        // Verify site.id was not changed
        assertEquals("predefined-id", configProperties.getProperty("site.id"));
    }

    @Test
    void testRunProcessesSiteId() throws IOException {
        // Setup parameters
        ConfigurationParameters parameters = new ConfigurationParameters(
                propertiesFile.getAbsolutePath(), // Use our test properties file
                "dummy", // Not used in this test
                true     // verbose
        );

        // Create a subclass that overrides parts we don't want to test
        ConfigAutomationApplication testApp = new ConfigAutomationApplication() {
            @Override
            public int run(ConfigurationParameters params) {
                try {
                    // Only run the part we're testing (load config + process runtime props)
                    ConfigProperties props = new ConfigProperties();
                    props.loadFromFile(params.getConfigFilePath());

                    // Call the method we want to test using reflection
                    Method processRuntimePropertiesMethod = ConfigAutomationApplication.class.getDeclaredMethod(
                            "processRuntimeProperties", ConfigProperties.class, boolean.class);
                    processRuntimePropertiesMethod.setAccessible(true);
                    processRuntimePropertiesMethod.invoke(this, props, params.isVerbose());

                    // Check if site.id was set correctly
                    String siteId = props.getProperty("site.id");
                    if (siteId == null || !siteId.equals("pcolocal18080sso")) {
                        return 1; // Test failed
                    }

                    return 0; // Test succeeded
                } catch (Exception e) {
                    return 1; // Test failed
                }
            }
        };

        // Execute and verify
        assertEquals(0, testApp.run(parameters));
    }
}
