package identity.am.automation;

import identity.am.automation.client.ApiClient;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.exception.WorkflowException;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStepFactory;
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
            WorkflowEngine workflowEngine = getWorkflowEngine(parameters);

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

    private static WorkflowEngine getWorkflowEngine(ConfigurationParameters parameters) throws IOException {
        // Load configuration properties
        ConfigProperties configProperties = new ConfigProperties();
        configProperties.loadFromFile(parameters.getConfigFilePath());

        // Enable verbose logging if requested
        if (parameters.isVerbose()) {
            log.info("Verbose mode enabled");
            // This would typically be handled by adjusting log levels in logback.xml dynamically
        }

        // Create API client
        ApiClient apiClient = new ApiClient(configProperties);

        // Create workflow engine
        WorkflowEngine workflowEngine = new WorkflowEngine(apiClient, configProperties);

        // Register workflow steps using the factory with modular providers
        WorkflowStepFactory stepFactory = new WorkflowStepFactory(configProperties);
        stepFactory.registerWorkflowSteps(workflowEngine);

        return workflowEngine;
    }
}