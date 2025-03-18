package com.pingidentity.pingam.workflow.provider;

import com.pingidentity.pingam.workflow.WorkflowEngine;

/**
 * Interface for workflow step providers
 * Each domain-specific provider will implement this interface to register its steps
 */
public interface StepProvider {

    /**
     * Register steps with the workflow engine
     *
     * @param workflowEngine The workflow engine to register steps with
     */
    void registerSteps(WorkflowEngine workflowEngine);
}