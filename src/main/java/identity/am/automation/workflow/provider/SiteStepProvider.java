package identity.am.automation.workflow.provider;

import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.site.CreateSiteRequest;
import identity.am.automation.model.site.CreateSiteResponse;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider for site-related workflow steps
 */
@Slf4j
@RequiredArgsConstructor
public class SiteStepProvider implements StepProvider {

    private final ConfigProperties configProperties;

    @Override
    public void registerSteps(WorkflowEngine workflowEngine) {
        // Create Site step
        WorkflowStep<CreateSiteRequest, CreateSiteResponse> createSiteStep =
                new WorkflowStep<>("createSite",
                        CreateSiteRequest.createDefault(configProperties),
                        CreateSiteResponse.class);

        createSiteStep
                .withSuccessHandler((response, props) -> {
                    log.info("Site configuration created:");
                    log.info("  Site ID: {}", response.getId());
                    log.info("  Primary URL: {}", response.getUrl());
                    if (response.getSecondaryURLs() != null && !response.getSecondaryURLs().isEmpty()) {
                        log.info("  Secondary URLs: {}", String.join(", ", response.getSecondaryURLs()));
                    }
                })
                .withDataExtractor("site.id", CreateSiteResponse::getId)
                .withDefaultNextStep("getRealmAliases");

        workflowEngine.registerStep(createSiteStep);

        log.debug("Registered site workflow steps");
    }
}