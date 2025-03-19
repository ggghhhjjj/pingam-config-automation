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
                .withDefaultNextStep("getServerAdvancedProperties");  // Changed to go to advanced properties step

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
                .withDefaultNextStep("getServerAdvancedProperties");  // Changed to go to advanced properties step

        workflowEngine.registerStep(updateServerPropertiesStep);

        // Create the step to get server advanced properties
        WorkflowStep<GetServerAdvancedPropertiesRequest, GetServerAdvancedPropertiesResponse> getServerAdvancedPropertiesStep =
                new WorkflowStep<>("getServerAdvancedProperties",
                        GetServerAdvancedPropertiesRequest.createDefault(),
                        GetServerAdvancedPropertiesResponse.class);

        getServerAdvancedPropertiesStep
                .withSuccessHandler((response, props) -> {
                    // Log current advanced properties values
                    Object currentLbCookieValue = response.getLbCookieValue();
                    Object currentReplicationPort = response.getReplicationPort();
                    log.info("Current LB cookie value: {}", currentLbCookieValue);
                    log.info("Current replication port: {}", currentReplicationPort);

                    // Store the advanced properties as JSON for the update request
                    try {
                        String propertiesJson = objectMapper.writeValueAsString(response.getProperties());
                        props.setRuntimeProperty("server.advanced.properties.json", propertiesJson);
                        log.debug("Stored server advanced properties JSON for update");
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing server advanced properties: {}", e.getMessage());
                    }

                    // Determine required values
                    String targetLbCookieValue = props.getProperty("server.lbcookie.value", "web1");
                    String targetReplicationPort = props.getProperty("server.replication.port", "58989");

                    // Determine if update is needed
                    boolean lbCookieUpdateNeeded = !targetLbCookieValue.equals(String.valueOf(currentLbCookieValue));
                    boolean replicationPortUpdateNeeded = !targetReplicationPort.equals(String.valueOf(currentReplicationPort));
                    boolean updateNeeded = lbCookieUpdateNeeded || replicationPortUpdateNeeded;

                    props.setRuntimeProperty("server.advancedPropertiesUpdateNeeded", String.valueOf(updateNeeded));

                    if (updateNeeded) {
                        log.info("Server advanced properties need to be updated:");
                        if (lbCookieUpdateNeeded) {
                            log.info("  - LB cookie value: {} -> {}", currentLbCookieValue, targetLbCookieValue);
                        }
                        if (replicationPortUpdateNeeded) {
                            log.info("  - Replication port: {} -> {}", currentReplicationPort, targetReplicationPort);
                        }
                    } else {
                        log.info("Server advanced properties already have correct values");
                    }
                })
                .withConditionalNextStep(
                        response -> "true".equals(configProperties.getProperty("server.advancedPropertiesUpdateNeeded")),
                        "updateServerAdvancedProperties")
                .withDefaultNextStep(WorkflowStep.END);  // End workflow if no update needed

        workflowEngine.registerStep(getServerAdvancedPropertiesStep);

        // Create the step to update server advanced properties
        WorkflowStep<UpdateServerAdvancedPropertiesRequest, UpdateServerAdvancedPropertiesResponse> updateServerAdvancedPropertiesStep =
                new WorkflowStep<>("updateServerAdvancedProperties",
                        UpdateServerAdvancedPropertiesRequest.createDefault(),
                        UpdateServerAdvancedPropertiesResponse.class);

        updateServerAdvancedPropertiesStep
                .withSuccessHandler((response, props) -> {
                    log.info("Successfully updated server advanced properties");
                    log.info("Updated LB cookie value: {}", response.getUpdatedLbCookieValue());
                    log.info("Updated replication port: {}", response.getUpdatedReplicationPort());
                })
                .withDefaultNextStep(WorkflowStep.END);  // End workflow after update

        workflowEngine.registerStep(updateServerAdvancedPropertiesStep);

        log.info("Registered server workflow steps");
    }
}
