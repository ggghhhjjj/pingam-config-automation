package com.pingidentity.pingam;

import com.pingidentity.pingam.client.ApiClient;
import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.exception.WorkflowException;
import com.pingidentity.pingam.model.auth.AuthenticationRequest;
import com.pingidentity.pingam.model.auth.AuthenticationResponse;
import com.pingidentity.pingam.model.realm.CreateRealmRequest;
import com.pingidentity.pingam.model.realm.CreateRealmResponse;
import com.pingidentity.pingam.model.realm.ListRealmsRequest;
import com.pingidentity.pingam.model.realm.ListRealmsResponse;
import com.pingidentity.pingam.model.ldap.CreateLdapConfigRequest;
import com.pingidentity.pingam.model.ldap.CreateLdapConfigResponse;
import com.pingidentity.pingam.model.ldap.TestLdapConnectionRequest;
import com.pingidentity.pingam.model.ldap.TestLdapConnectionResponse;
import com.pingidentity.pingam.workflow.WorkflowEngine;
import com.pingidentity.pingam.workflow.WorkflowStep;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Main class for the PingAm configuration automation application
 */
@Slf4j
public class ConfigAutomationApplication {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar pingam-config-automation.jar <properties-file>");
            System.exit(1);
        }

        String propertiesFile = args[0];

        try {
            // Initialize configuration
            ConfigProperties configProperties = new ConfigProperties();
            configProperties.loadFromFile(propertiesFile);

            // Create API client
            ApiClient apiClient = new ApiClient(configProperties);

            // Create workflow engine
            WorkflowEngine workflowEngine = new WorkflowEngine(apiClient, configProperties);

            // Register workflow steps
            registerWorkflowSteps(workflowEngine);

            // Execute the workflow starting with authentication
            String startStep = args.length > 1 ? args[1] : "authenticate";
            workflowEngine.execute(startStep);

            log.info("PingAm configuration completed successfully");

        } catch (IOException e) {
            log.error("Error loading configuration: {}", e.getMessage(), e);
            System.exit(1);
        } catch (WorkflowException e) {
            log.error("Error executing workflow: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Register all workflow steps with the workflow engine
     */
    private static void registerWorkflowSteps(WorkflowEngine workflowEngine) {
        // Authentication step
        WorkflowStep<AuthenticationRequest, AuthenticationResponse> authenticateStep =
                new WorkflowStep<>("authenticate",
                        AuthenticationRequest.createDefault(),
                        AuthenticationResponse.class);

        authenticateStep
                .withDataExtractor("auth.token", AuthenticationResponse::getTokenId)
                .withDefaultNextStep("createRealm");

        workflowEngine.registerStep(authenticateStep);

        // Create realm step
        WorkflowStep<CreateRealmRequest, CreateRealmResponse> createRealmStep =
                new WorkflowStep<>("createRealm",
                        CreateRealmRequest.createDefault("${realm.name}"),
                        CreateRealmResponse.class);

        createRealmStep
                .withDataExtractor("realm.id", CreateRealmResponse::getId)
                .withDefaultNextStep("listRealms");

        workflowEngine.registerStep(createRealmStep);

        // List realms step
        WorkflowStep<ListRealmsRequest, ListRealmsResponse> listRealmsStep =
                new WorkflowStep<>("listRealms",
                        ListRealmsRequest.createDefault(),
                        ListRealmsResponse.class);

        listRealmsStep
                .withSuccessHandler((response, props) -> {
                    log.info("Found {} realms", response.getResultCount());
                    response.getResult().forEach(realm -> {
                        log.info("Realm: {}", realm.get("name"));
                    });
                })
                .withDefaultNextStep("testLdapConnection");

        workflowEngine.registerStep(listRealmsStep);

        // Test LDAP connection step
        WorkflowStep<TestLdapConnectionRequest, TestLdapConnectionResponse> testLdapStep =
                new WorkflowStep<>("testLdapConnection",
                        TestLdapConnectionRequest.createDefault(),
                        TestLdapConnectionResponse.class);

        testLdapStep
                .withSuccessHandler((response, props) -> {
                    log.info("LDAP connection test: {}", response.getMessage());
                })
                .withConditionalNextStep(response -> response.isValid(), "createLdapConfig")
                .withConditionalNextStep(response -> !response.isValid(), null); // End workflow if LDAP test fails

        workflowEngine.registerStep(testLdapStep);

        // Create LDAP configuration step
        WorkflowStep<CreateLdapConfigRequest, CreateLdapConfigResponse> createLdapStep =
                new WorkflowStep<>("createLdapConfig",
                        CreateLdapConfigRequest.createDefault(),
                        CreateLdapConfigResponse.class);

        createLdapStep
                .withSuccessHandler((response, props) -> {
                    log.info("LDAP configuration created: {} on {}", response.getName(), response.getHost());
                });

        workflowEngine.registerStep(createLdapStep);
    }
}