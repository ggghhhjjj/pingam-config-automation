package identity.am.automation.workflow.provider;

import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.HttpMethod;
import identity.am.automation.model.realm.GetRealmsRequest;
import identity.am.automation.model.realm.GetRealmsResponse;
import identity.am.automation.model.realm.RealmInfo;
import identity.am.automation.model.realm.UpdateRealmRequest;
import identity.am.automation.model.realm.UpdateRealmResponse;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provider for realm-related workflow steps
 */
@Slf4j
@RequiredArgsConstructor
public class RealmStepProvider implements StepProvider {

    private final ConfigProperties configProperties;

    @Override
    public void registerSteps(WorkflowEngine workflowEngine) {
        // Create the step to get all realms
        WorkflowStep<GetRealmsRequest, GetRealmsResponse> getRealmStep =
                new WorkflowStep<>("getRealmAliases",
                        GetRealmsRequest.createDefault(),
                        GetRealmsResponse.class);

        // Add handler to process the response
        getRealmStep
                .withDataExtractor("realm.topLevelId", response -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    return topLevelRealm != null ? topLevelRealm.getId() : null;
                })
                .withDataExtractor("realm.topLevelName", response -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    return topLevelRealm != null ? topLevelRealm.getName() : null;
                })
                .withDataExtractor("realm.topLevelActive", response -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    return topLevelRealm != null ? String.valueOf(topLevelRealm.isActive()) : "true";
                })
                .withDataExtractor("realm.topLevelParentPath", response -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    return topLevelRealm != null ? topLevelRealm.getParentPath() : "";
                })
                .withDataExtractor("realm.topLevelAliases", response -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    if (topLevelRealm != null && topLevelRealm.getAliases() != null) {
                        return String.join(",", topLevelRealm.getAliases());
                    }
                    return "";
                })
                .withSuccessHandler((response, props) -> {
                    RealmInfo topLevelRealm = response.getTopLevelRealm();
                    if (topLevelRealm != null) {
                        log.info("Found top level realm: {} (ID: {})", topLevelRealm.getName(), topLevelRealm.getId());

                        // Get required host aliases
                        Set<String> requiredAliases = getRequiredHostAliases(props);

                        // Get current aliases
                        Set<String> currentAliases = new HashSet<>();
                        if (topLevelRealm.getAliases() != null) {
                            currentAliases.addAll(topLevelRealm.getAliases());
                        }

                        // Check if updates are needed
                        boolean updateNeeded = false;
                        for (String alias : requiredAliases) {
                            if (!currentAliases.contains(alias)) {
                                currentAliases.add(alias);
                                updateNeeded = true;
                                log.info("Need to add alias: {}", alias);
                            }
                        }

                        // Set update flag
                        props.setRuntimeProperty("realm.updateRequired", String.valueOf(updateNeeded));

                        // Store updated aliases
                        if (updateNeeded) {
                            props.setRuntimeProperty("realm.updatedAliases",
                                    String.join(",", currentAliases));
                        }
                    } else {
                        log.warn("No top level realm found in response");
                        props.setRuntimeProperty("realm.updateRequired", "false");
                    }
                })
                .withConditionalNextStep(
                        response -> "true".equals(configProperties.getProperty("realm.updateRequired")),
                        "updateRealmAliases")
                .withConditionalNextStep(
                        response -> !"true".equals(configProperties.getProperty("realm.updateRequired")),
                        "getServers");

        // Register the get realms step
        workflowEngine.registerStep(getRealmStep);

        // Create the step to update realm with new aliases if needed
        WorkflowStep<UpdateRealmRequest, UpdateRealmResponse> updateRealmStep =
                new WorkflowStep<>("updateRealmAliases",
                        createUpdateRealmRequest(),
                        UpdateRealmResponse.class);

        // Add handler for update step
        updateRealmStep
                .withSuccessHandler((response, props) -> {
                    log.info("Successfully updated realm aliases");
                    log.info("Updated realm: {} (ID: {})", response.getName(), response.getId());
                    log.info("New aliases: {}", response.getAliases());
                })
                .withDefaultNextStep("getServers");  // End workflow after update

        // Register the update realm step
        workflowEngine.registerStep(updateRealmStep);

        log.info("Registered realm workflow steps");
    }

    /**
     * Create the update realm request with configuration from runtime properties
     */
    private UpdateRealmRequest createUpdateRealmRequest() {
        return new UpdateRealmRequest() {
            @Override
            public UpdateRealmRequest updatePlaceholders(ConfigProperties configProperties) {
                // First apply standard placeholder replacement
                super.updatePlaceholders(configProperties);

                // Then dynamically build the request using the runtime properties
                String realmId = configProperties.getProperty("realm.topLevelId");
                String realmName = configProperties.getProperty("realm.topLevelName");
                boolean active = Boolean.parseBoolean(configProperties.getProperty("realm.topLevelActive", "true"));
                String parentPath = configProperties.getProperty("realm.topLevelParentPath", "");

                // Get the updated aliases
                String aliasesStr = configProperties.getProperty("realm.updatedAliases", "");
                List<String> aliases = new ArrayList<>();
                if (!aliasesStr.isEmpty()) {
                    aliases = Arrays.asList(aliasesStr.split(","));
                }

                // Update this request with the dynamic parameters
                this.setEndpoint("/json/global-config/realms/" + realmId);
                this.setMethod(HttpMethod.PUT);
                this.setName(realmName);
                this.setActive(active);
                this.setParentPath(parentPath);
                this.setAliases(aliases);

                // Add required headers
                this.withHeader("iPlanetDirectoryPro", configProperties.getProperty("auth.token"));
                this.withHeader("Accept-API-Version", "protocol=2.0,resource=1.0");

                return this;
            }
        };
    }

    /**
     * Extract required host aliases from site URL and secondary URLs
     */
    private Set<String> getRequiredHostAliases(ConfigProperties props) {
        Set<String> aliases = new HashSet<>();

        // Get primary site URL
        String siteUrl = props.getProperty("site.url");
        if (siteUrl != null && !siteUrl.isEmpty()) {
            String host = extractHostname(siteUrl);
            if (host != null) {
                aliases.add(host);
            }
        }

        // Get secondary site URLs
        String secondaryUrlsStr = props.getProperty("site.secondaryUrls");
        if (secondaryUrlsStr != null && !secondaryUrlsStr.isEmpty()) {
            String[] secondaryUrls = secondaryUrlsStr.split(",");
            for (String url : secondaryUrls) {
                String host = extractHostname(url.trim());
                if (host != null) {
                    aliases.add(host);
                }
            }
        }

        return aliases;
    }

    /**
     * Extract hostname from a URL
     */
    private String extractHostname(String url) {
        try {
            // Add scheme if missing
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            URI uri = new URI(url);
            String host = uri.getHost();

            // If port is specified in the host, remove it
            if (host != null && host.contains(":")) {
                host = host.substring(0, host.indexOf(":"));
            }

            return host;
        } catch (URISyntaxException e) {
            log.warn("Invalid URL format: {}", url);
            return null;
        }
    }
}