package com.pingidentity.pingam.workflow;

import com.pingidentity.pingam.model.auth.AuthenticationRequest;
import com.pingidentity.pingam.model.auth.AuthenticationResponse;
import com.pingidentity.pingam.model.site.CreateSiteRequest;
import com.pingidentity.pingam.model.site.CreateSiteResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating and registering workflow steps
 * Responsible for defining the workflow structure
 */
@Slf4j
public class WorkflowStepFactory {

    /**
     * Register all workflow steps with the workflow engine
     * @param workflowEngine The engine to register steps with
     */
    public void registerWorkflowSteps(WorkflowEngine workflowEngine) {
        // Authentication step
        WorkflowStep<AuthenticationRequest, AuthenticationResponse> authenticateStep =
                new WorkflowStep<>("authenticate",
                        AuthenticationRequest.createDefault(),
                        AuthenticationResponse.class);

        authenticateStep
                .withDataExtractor("auth.token", AuthenticationResponse::getTokenId)
                .withDefaultNextStep("createRealm");

        workflowEngine.registerStep(authenticateStep);

        // Create Site step
        WorkflowStep<CreateSiteRequest, CreateSiteResponse> createSiteStep =
                new WorkflowStep<>("createSite",
                        CreateSiteRequest.createDefault(),
                        CreateSiteResponse.class);

        createSiteStep
                .withSuccessHandler((response, props) -> {
                    log.info("Site configuration created:");
                    log.info("  Site ID: {}", response.getId());
                    log.info("  Primary URL: {}", response.getUrl());
                    if (response.getSecondaryURLs() != null && !response.getSecondaryURLs().isEmpty()) {
                        log.info("  Secondary URLs: {}", String.join(", ", response.getSecondaryURLs()));
                    }
                });

        workflowEngine.registerStep(createSiteStep);
    }
}