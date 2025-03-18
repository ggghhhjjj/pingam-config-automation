package com.pingidentity.pingam.workflow.provider;

import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.model.site.CreateSiteRequest;
import com.pingidentity.pingam.model.site.CreateSiteResponse;
import com.pingidentity.pingam.workflow.WorkflowEngine;
import com.pingidentity.pingam.workflow.WorkflowStep;
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
                .withDataExtractor("site.id", CreateSiteResponse::getId);
        // No next step defined as this is currently the end of the workflow

        workflowEngine.registerStep(createSiteStep);

        log.debug("Registered site workflow steps");
    }
}