package identity.am.automation.workflow.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.server.*;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider for server-related workflow steps
 */
@Slf4j
@RequiredArgsConstructor
public class ServerStepProvider implements StepProvider {

    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void registerSteps(WorkflowEngine workflowEngine) {
        // Create the step to get all servers
        WorkflowStep<GetServersRequest, GetServersResponse> getServersStep =
                new WorkflowStep<>("getServers",
                        GetServersRequest.createDefault(),
                        GetServersResponse.class);

        // Add handler to process the response
        getServersStep
                .withSuccessHandler((response, props) -> {
                    // Get the base URL from configuration
                    String baseUrl = props.resolveBaseUrl();
                    log.info("Looking for server with URL: {}", baseUrl);

                    // Find the server with matching URL
                    ServerInfo server = response.findServerByUrl(baseUrl);
                    if (server != null) {
                        log.info("Found server: {} (ID: {})", server.getUrl(), server.getId());

                        // Store the server ID for subsequent steps
                        props.setRuntimeProperty("server.id", server.getId());
                        props.setRuntimeProperty("server.url", server.getUrl());
                        props.setRuntimeProperty("server.currentSiteName",
                                server.getSiteName() != null ? server.getSiteName() : "");
                    } else {
                        log.warn("No server found with URL: {}", baseUrl);
                        // Set a flag to indicate we should skip server property steps
                        props.setRuntimeProperty("server.skip", "true");
                    }
                })
                .withConditionalNextStep(
                        response -> "true".equals(configProperties.getProperty("server.skip")),
                        WorkflowStep.END) // End workflow if no server found
                .withDefaultNextStep("getServerProperties");

        workflowEngine.registerStep(getServersStep);

        // Create the step to get server properties
        WorkflowStep<GetServerPropertiesRequest, GetServerPropertiesResponse> getServerPropertiesStep =
                new WorkflowStep<>("getServerProperties",
                        GetServerPropertiesRequest.createDefault(),
                        GetServerPropertiesResponse.class);

        getServerPropertiesStep
                .withSuccessHandler((response, props) -> {
                    // Log the current site value
                    String currentSiteValue = response.getCurrentSiteValue();
                    log.info("Current server site value: {}", currentSiteValue);

                    // Store the current site value
                    props.setRuntimeProperty("server.currentSiteValue", currentSiteValue);

                    // Store the entire properties map as JSON for the update request
                    try {
                        String propertiesJson = objectMapper.writeValueAsString(response.getProperties());
                        props.setRuntimeProperty("server.properties.json", propertiesJson);
                        log.debug("Stored server properties JSON for update");
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing server properties: {}", e.getMessage());
                    }

                    // Determine if an update is needed
                    String targetSiteId = props.getProperty("site.id");
                    boolean updateNeeded = !targetSiteId.equals(currentSiteValue);
                    props.setRuntimeProperty("server.propertiesUpdateNeeded", String.valueOf(updateNeeded));

                    if (updateNeeded) {
                        log.info("Server properties need to be updated with site: {}", targetSiteId);
                    } else {
                        log.info("Server properties already have correct site: {}", targetSiteId);
                    }
                })
                .withConditionalNextStep(
                        response -> "true".equals(configProperties.getProperty("server.propertiesUpdateNeeded")),
                        "updateServerProperties")
                .withDefaultNextStep(WorkflowStep.END);  // End workflow if no update needed

        workflowEngine.registerStep(getServerPropertiesStep);

        // Create the step to update server properties
        WorkflowStep<UpdateServerPropertiesRequest, UpdateServerPropertiesResponse> updateServerPropertiesStep =
                new WorkflowStep<>("updateServerProperties",
                        UpdateServerPropertiesRequest.createDefault(),
                        UpdateServerPropertiesResponse.class);

        updateServerPropertiesStep
                .withSuccessHandler((response, props) -> {
                    String updatedSiteValue = response.getUpdatedSiteValue();
                    log.info("Successfully updated server properties");
                    log.info("Updated site value: {}", updatedSiteValue);
                })
                .withDefaultNextStep(WorkflowStep.END);  // End workflow after update

        workflowEngine.registerStep(updateServerPropertiesStep);

        log.info("Registered server workflow steps");
    }
}