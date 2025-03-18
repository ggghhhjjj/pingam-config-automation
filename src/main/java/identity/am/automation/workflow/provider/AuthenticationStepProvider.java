package identity.am.automation.workflow.provider;

import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.auth.AuthenticationRequest;
import identity.am.automation.model.auth.AuthenticationResponse;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
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