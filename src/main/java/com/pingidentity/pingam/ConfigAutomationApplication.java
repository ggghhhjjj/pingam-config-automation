package com.pingidentity.pingam;

import com.pingidentity.pingam.client.ApiClient;
import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.exception.WorkflowException;
import com.pingidentity.pingam.workflow.WorkflowEngine;
import com.pingidentity.pingam.workflow.WorkflowStepFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Main application for the PingAm configuration automation
 * Responsible for orchestrating the configuration process
 */
@Slf4j
public class ConfigAutomationApplication {

    /**
     * Run the application with the provided parameters
     * @param parameters Configuration parameters
     * @return Exit code (0 for success, non-zero for failure)
     */
    public int run(ConfigurationParameters parameters) {
        try {
            // Initialize configuration
            ConfigProperties configProperties = new ConfigProperties();
            configProperties.loadFromFile(parameters.getConfigFilePath());

            // Process runtime properties
            processRuntimeProperties(configProperties, parameters.isVerbose());

            // Create API client
            ApiClient apiClient = new ApiClient(configProperties);

            // Create workflow engine
            WorkflowEngine workflowEngine = new WorkflowEngine(apiClient, configProperties);

            // Register workflow steps using the factory
            WorkflowStepFactory stepFactory = new WorkflowStepFactory();
            stepFactory.registerWorkflowSteps(workflowEngine);

            // Execute the workflow starting with the specified step
            workflowEngine.execute(parameters.getStartStep());

            log.info("PingAm configuration completed successfully");
            return 0;

        } catch (IOException e) {
            log.error("Error loading configuration: {}", e.getMessage(), e);
            return 1;
        } catch (WorkflowException e) {
            log.error("Error executing workflow: {}", e.getMessage(), e);
            return 1;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Process and set up any runtime properties that need to be derived from configuration
     * @param configProperties The loaded configuration properties
     * @param verbose Whether to log verbose information
     */
    private void processRuntimeProperties(ConfigProperties configProperties, boolean verbose) {
        // Generate site ID from site URL if not explicitly set
        String siteId = configProperties.getProperty("site.id");
        if (siteId == null || siteId.isEmpty()) {
            String siteUrl = configProperties.getProperty("site.url");
            if (siteUrl != null && !siteUrl.isEmpty()) {
                // Generate a site ID by removing non-alphanumeric characters from the URL
                siteId = generateSiteIdFromUrl(siteUrl);

                // Set the generated ID as a runtime property
                configProperties.setRuntimeProperty("site.id", siteId);

                if (verbose) {
                    log.info("Automatically generated site.id '{}' from site.url '{}'", siteId, siteUrl);
                }
            }
        }
    }

    /**
     * Generate a site ID from a URL by removing all non-alphanumeric characters
     * @param url The URL to generate an ID from
     * @return The generated alphanumeric-only ID
     */
    private String generateSiteIdFromUrl(String url) {
        // Remove protocol (http://, https://)
        String result = url.replaceAll("^(http|https)://", "");

        // Remove all non-alphanumeric characters
        result = result.replaceAll("[^a-zA-Z0-9]", "");

        // Limit length if needed (optional, adjust max length as appropriate)
        final int maxLength = 30;
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }

        return result;
    }
}