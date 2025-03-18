package identity.am.automation.workflow;

import identity.am.automation.client.ApiClient;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.exception.ApiException;
import identity.am.automation.exception.WorkflowException;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.ApiResponse;
import identity.am.automation.model.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private ConfigProperties configProperties;

    private WorkflowEngine workflowEngine;

    @BeforeEach
    void setUp() {
        workflowEngine = new WorkflowEngine(apiClient, configProperties);
    }

    @Test
    void testBasicWorkflowExecution() throws ApiException, WorkflowException {
        // Create test workflow steps
        TestRequest request1 = TestRequest.builder().endpoint("/test1").method(HttpMethod.GET).build();
        TestResponse response1 = new TestResponse("result1", true);

        TestRequest request2 = TestRequest.builder().endpoint("/test2").method(HttpMethod.POST).build();
        TestResponse response2 = new TestResponse("result2", true);

        // Create workflow steps
        WorkflowStep<TestRequest, TestResponse> step1 =
                new WorkflowStep<>("step1", request1, TestResponse.class).withDefaultNextStep("step2");

        WorkflowStep<TestRequest, TestResponse> step2 = new WorkflowStep<>("step2", request2, TestResponse.class);

        // Register steps
        workflowEngine.registerStep(step1);
        workflowEngine.registerStep(step2);

        // Mock API client responses
        when(apiClient.execute(eq(request1), eq(TestResponse.class))).thenReturn(response1);
        when(apiClient.execute(eq(request2), eq(TestResponse.class))).thenReturn(response2);

        // Execute workflow
        workflowEngine.execute("step1");

        // Verify both steps were executed
        verify(apiClient).execute(eq(request1), eq(TestResponse.class));
        verify(apiClient).execute(eq(request2), eq(TestResponse.class));
    }

    @Test
    void testConditionalBranching() throws ApiException, WorkflowException {
        // Create test workflow steps and responses
        TestRequest requestA = TestRequest.builder().endpoint("/testA").method(HttpMethod.GET).build();
        TestResponse responseA = new TestResponse("resultA", true);

        TestRequest requestB = TestRequest.builder().endpoint("/testB").method(HttpMethod.GET).build();
        TestResponse responseB = new TestResponse("resultB", true);

        TestRequest requestC = TestRequest.builder().endpoint("/testC").method(HttpMethod.GET).build();
        TestResponse responseC = new TestResponse("resultC", true);

        // Create workflow steps with branching
        WorkflowStep<TestRequest, TestResponse> stepA =
                new WorkflowStep<>("stepA", requestA, TestResponse.class).withConditionalNextStep(response ->
                        "resultA".equals(response.getTestResult()), "stepB").withConditionalNextStep(response ->
                        "someOtherResult".equals(response.getTestResult()), "stepC");

        WorkflowStep<TestRequest, TestResponse> stepB = new WorkflowStep<>("stepB", requestB, TestResponse.class);

        WorkflowStep<TestRequest, TestResponse> stepC = new WorkflowStep<>("stepC", requestC, TestResponse.class);

        // Register steps
        workflowEngine.registerStep(stepA);
        workflowEngine.registerStep(stepB);
        workflowEngine.registerStep(stepC);

        // Mock API client responses
        when(apiClient.execute(eq(requestA), eq(TestResponse.class))).thenReturn(responseA);
        when(apiClient.execute(eq(requestB), eq(TestResponse.class))).thenReturn(responseB);

        // Execute workflow
        workflowEngine.execute("stepA");

        // Verify only steps A and B were executed, not C (due to condition)
        verify(apiClient).execute(eq(requestA), eq(TestResponse.class));
        verify(apiClient).execute(eq(requestB), eq(TestResponse.class));
        verify(apiClient, never()).execute(eq(requestC), eq(TestResponse.class));
    }

    @Test
    void testDataExtraction() throws ApiException, WorkflowException {
        // Create test workflow step
        TestRequest request = TestRequest.builder().endpoint("/test").method(HttpMethod.GET).build();
        TestResponse response = new TestResponse("extractMe", true);

        // Create workflow step with data extraction
        WorkflowStep<TestRequest, TestResponse> step =
                new WorkflowStep<>("step", request, TestResponse.class).withDataExtractor("extracted.value",
                        TestResponse::getTestResult);

        // Register step
        workflowEngine.registerStep(step);

        // Mock API client responses
        when(apiClient.execute(eq(request), eq(TestResponse.class))).thenReturn(response);

        // Execute workflow
        workflowEngine.execute("step");

        // Verify data extraction
        verify(configProperties).setRuntimeProperty("extracted.value", "extractMe");
    }

    @Test
    void testStepNotFound() {
        // Test and verify
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            workflowEngine.execute("nonExistentStep");
        });

        assertTrue(exception.getMessage().contains("Step not found"));
    }

    @Test
    void testApiException() throws ApiException {
        // Create test workflow step
        TestRequest request = TestRequest.builder().endpoint("/test").method(HttpMethod.GET).build();

        // Create workflow step
        WorkflowStep<TestRequest, TestResponse> step = new WorkflowStep<>("step", request, TestResponse.class);

        // Register step
        workflowEngine.registerStep(step);

        // Mock API client to throw exception
        when(apiClient.execute(eq(request), eq(TestResponse.class))).thenThrow(new ApiException("API error"));

        // Test and verify
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            workflowEngine.execute("step");
        });

        assertTrue(exception.getMessage().contains("Error executing step"));
        assertInstanceOf(ApiException.class, exception.getCause());
    }

    // Test request/response classes
    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestRequest extends ApiRequest {
        private String testField;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestResponse extends ApiResponse {
        private String testResult;
        private boolean valid;

        @Override
        public boolean isValid() {
            return valid;
        }
    }
}