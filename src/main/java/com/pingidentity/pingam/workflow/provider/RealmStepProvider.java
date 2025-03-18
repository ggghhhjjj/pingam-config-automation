package com.pingidentity.pingam.workflow.provider;

import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.workflow.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider for realm-related workflow steps
 * This is a placeholder for future implementation
 */
@Slf4j
@RequiredArgsConstructor
public class RealmStepProvider implements StepProvider {

    private final ConfigProperties configProperties;

    @Override
    public void registerSteps(WorkflowEngine workflowEngine) {
        // TODO: Implement realm-related workflow steps
        // Examples might include:
        // - CreateRealmStep
        // - ConfigureRealmStep
        // - AddLDAPToRealmStep

        log.debug("Realm workflow steps not yet implemented");
    }
}