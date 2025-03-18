package com.pingidentity.pingam.workflow.provider;

import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.model.auth.AuthenticationRequest;
import com.pingidentity.pingam.model.auth.AuthenticationResponse;
import com.pingidentity.pingam.workflow.WorkflowEngine;
import com.pingidentity.pingam.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider for authentication-related workflow steps
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationStepProvider implements StepProvider {

    private final ConfigProperties configProperties;

    @Override
    public void registerSteps(WorkflowEngine workflowEngine) {
        // Authentication step
        WorkflowStep<AuthenticationRequest, AuthenticationResponse> authenticateStep =
                new WorkflowStep<>("authenticate",
                        AuthenticationRequest.createDefault(configProperties),
                        AuthenticationResponse.class);

        authenticateStep
                .withDataExtractor("auth.token", AuthenticationResponse::getTokenId)
                .withDefaultNextStep("createSite"); // This will be the default next step

        workflowEngine.registerStep(authenticateStep);

        log.debug("Registered authentication workflow steps");
    }
}