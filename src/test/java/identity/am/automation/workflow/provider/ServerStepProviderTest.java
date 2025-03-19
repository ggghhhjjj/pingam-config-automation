package identity.am.automation.workflow.provider;

import identity.am.automation.config.ConfigProperties;
import identity.am.automation.workflow.WorkflowEngine;
import identity.am.automation.workflow.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Set strict stubbing to false to allow unused stubs
@ExtendWith(MockitoExtension.class)
class ServerStepProviderTest {

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private WorkflowEngine workflowEngine;

    @Captor
    private ArgumentCaptor<WorkflowStep<?, ?>> workflowStepCaptor;

    private ServerStepProvider serverStepProvider;

    @BeforeEach
    void setUp() {
        serverStepProvider = new ServerStepProvider(configProperties);

        // Set up common mocks in lenient mode
        lenient().when(configProperties.getProperty("server.id")).thenReturn("server-1");
        lenient().when(configProperties.getProperty("auth.token")).thenReturn("test-token");

        // Default return for any other property
        lenient().when(configProperties.getProperty(anyString())).thenReturn(null);
    }

    @Test
    void testRegisterStepsWithWorkflowEngine() {
        // Execute the method
        serverStepProvider.registerSteps(workflowEngine);

        // Verify that five steps were registered (updated from three to include the two new advanced property steps)
        verify(workflowEngine, times(5)).registerStep(workflowStepCaptor.capture());

        // Get the captured steps
        List<WorkflowStep<?, ?>> capturedSteps = workflowStepCaptor.getAllValues();
        assertEquals(5, capturedSteps.size());

        // Verify step names
        assertEquals("getServers", capturedSteps.get(0).getName());
        assertEquals("getServerProperties", capturedSteps.get(1).getName());
        assertEquals("updateServerProperties", capturedSteps.get(2).getName());
        assertEquals("getServerAdvancedProperties", capturedSteps.get(3).getName());
        assertEquals("updateServerAdvancedProperties", capturedSteps.get(4).getName());
    }

    // Keep your other existing test methods below
}