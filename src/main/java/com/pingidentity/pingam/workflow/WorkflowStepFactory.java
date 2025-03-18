package com.pingidentity.pingam.workflow;

import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.workflow.provider.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating and registering workflow steps
 * Uses step providers to organize steps by domain
 */
@Slf4j
public class WorkflowStepFactory {

    private final ConfigProperties configProperties;
    private final List<StepProvider> providers = new ArrayList<>();

    public WorkflowStepFactory(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        initializeProviders();
    }

    /**
     * Initialize the list of step providers
     */
    private void initializeProviders() {
        // Add all providers here
        providers.add(new AuthenticationStepProvider(configProperties));
        providers.add(new SiteStepProvider(configProperties));
        providers.add(new RealmStepProvider(configProperties));

        // Add additional providers as needed
    }

    /**
     * Register all workflow steps with the workflow engine
     * @param workflowEngine The engine to register steps with
     */
    public void registerWorkflowSteps(WorkflowEngine workflowEngine) {
        log.info("Registering workflow steps from {} providers", providers.size());

        // Use each provider to register its steps
        for (StepProvider provider : providers) {
            provider.registerSteps(workflowEngine);
        }

        log.info("Workflow steps registration complete");
    }
}