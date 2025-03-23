package identity.am.automation.config;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handles configuration properties loaded from properties files
 */
@Slf4j
public class ConfigProperties {
    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, String> runtimeValues = new HashMap<>();

    /**
     * Load properties from a file
     * @param filePath Path to the properties file
     * @throws IOException If there's an error reading the file
     */
    public void loadFromFile(String filePath) throws IOException {
        log.info("Loading configuration from file: {}", filePath);
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(filePath)) {
            props.load(input);

            props.forEach((key, value) -> properties.put(key.toString(), value.toString()));
        }

        resolveDefaultValue();

        log.info("Loaded {} properties", properties.size());
    }

    /**
     * Get a property value
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        // First check runtime values, which have precedence
        String runtimeValue = runtimeValues.get(key);
        if (runtimeValue != null) {
            return runtimeValue;
        }

        return properties.get(key);
    }

    /**
     * Get a property value with a default fallback
     * @param key The property key
     * @param defaultValue The default value to return if property not found
     * @return The property value or defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Set a runtime property value that overrides file properties
     * Useful for passing values between API calls
     * @param key The property key
     * @param value The property value
     */
    public void setRuntimeProperty(String key, String value) {
        runtimeValues.put(key, value);
    }

    /**
     * Clear all runtime property values
     */
    public void clearRuntimeProperties() {
        runtimeValues.clear();
    }

    /**
     * Get all properties (file properties + runtime properties)
     * Runtime properties override file properties
     * @return Map of all properties
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> all = new HashMap<>(properties);
        all.putAll(runtimeValues);
        return all;
    }

    public String resolveBaseUrl() {
        return getProperty("api.baseUrl");
    }

    private void resolveDefaultValue() {
        properties.put("api.auth.cookie.name", properties.getOrDefault("api.auth.cookie.name", "iPlanetDirectoryPro"));
        properties.put("api.version", properties.getOrDefault("api.version", "resource=2.0,protocol=1.0"));
        properties.put("server.lbcookie.value",  properties.getOrDefault("server.lbcookie.value", "web1"));
        properties.put("server.replication.port",  properties.getOrDefault("server.replication.port", "58989"));
    }
}
