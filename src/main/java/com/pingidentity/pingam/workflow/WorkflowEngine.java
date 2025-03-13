package com.pingidentity.pingam.workflow;

import com.pingidentity.pingam.client.ApiClient;
import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.exception.ApiException;
import com.pingidentity.pingam.exception.WorkflowException;
import com.pingidentity.pingam.model.ApiRequest;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the execution of workflow steps
 */
@Slf4j
public class WorkflowEngine {
    private final Map<String, WorkflowStep<?, ?>> steps = new HashMap<>();
    private final ApiClient apiClient;
    private final ConfigProperties configProperties;

    public WorkflowEngine(ApiClient apiClient, ConfigProperties configProperties) {
        this.apiClient = apiClient;
        this.configProperties = configProperties;
    }

    /**
     * Register a step in the workflow
     */
    public <REQ extends ApiRequest, RESP extends ApiResponse> void registerStep(WorkflowStep<REQ, RESP> step) {
        steps.put(step.getName(), step);
    }

    /**
     * Execute the workflow starting from the given step
     */
    public void execute(String startStepName) throws WorkflowException {
        String currentStepName = startStepName;

        while (currentStepName != null) {
            WorkflowStep<?, ?> currentStep = steps.get(currentStepName);

            if (currentStep == null) {
                throw new WorkflowException("Step not found: " + currentStepName);
            }

            try {
                currentStepName = currentStep.execute(apiClient, configProperties);
            } catch (ApiException e) {
                throw new WorkflowException("Error executing step: " + currentStepName, e);
            }
        }

        log.info("Workflow completed successfully");
    }
}
