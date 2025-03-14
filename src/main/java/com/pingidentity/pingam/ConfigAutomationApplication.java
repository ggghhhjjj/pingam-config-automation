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
}