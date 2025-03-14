package com.pingidentity.pingam.workflow;

import com.pingidentity.pingam.model.auth.AuthenticationRequest;
import com.pingidentity.pingam.model.auth.AuthenticationResponse;
import com.pingidentity.pingam.model.ldap.CreateLdapConfigRequest;
import com.pingidentity.pingam.model.ldap.CreateLdapConfigResponse;
import com.pingidentity.pingam.model.ldap.TestLdapConnectionRequest;
import com.pingidentity.pingam.model.ldap.TestLdapConnectionResponse;
import com.pingidentity.pingam.model.realm.CreateRealmRequest;
import com.pingidentity.pingam.model.realm.CreateRealmResponse;
import com.pingidentity.pingam.model.realm.ListRealmsRequest;
import com.pingidentity.pingam.model.realm.ListRealmsResponse;
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