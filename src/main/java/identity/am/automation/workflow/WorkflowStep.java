package identity.am.automation.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import identity.am.automation.client.ApiClient;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.exception.ApiException;
import identity.am.automation.exception.WorkflowException;
import identity.am.automation.model.ApiRequest;
import identity.am.automation.model.ApiResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a step in a workflow
 */
@Slf4j
public class WorkflowStep<REQ extends ApiRequest, RESP extends ApiResponse> {
    public static String END = "ENDS";

    @Getter
    private final String name;
    private final REQ request;
    private final Class<RESP> responseClass;
    private final List<ResponseHandler<RESP>> successHandlers = new ArrayList<>();
    private final Map<Predicate<RESP>, String> conditionalNextSteps = new HashMap<>();
    private String defaultNextStep;

    public WorkflowStep(String name, REQ request, Class<RESP> responseClass) {
        this.name = name;
        this.request = request;
        this.responseClass = responseClass;
    }

    /**
     * Add a handler to process the response when this step is successful
     */
    public WorkflowStep<REQ, RESP> withSuccessHandler(ResponseHandler<RESP> handler) {
        successHandlers.add(handler);
        return this;
    }

    /**
     * Add a handler to extract data from response and store in runtime properties
     */
    public WorkflowStep<REQ, RESP> withDataExtractor(String propertyName, Function<RESP, String> extractor) {
        return withSuccessHandler(new ResponseHandler<RESP>() {
            @Override
            public void handle(RESP response, ConfigProperties configProperties) {
                String value = extractor.apply(response);
                if (value != null) {
                    configProperties.setRuntimeProperty(propertyName, value);
                    log.debug("Extracted property {}={} from response", propertyName, value);
                }
            }
        });
    }

    /**
     * Set the default next step if no conditional transitions match
     */
    public WorkflowStep<REQ, RESP> withDefaultNextStep(String nextStep) {
        this.defaultNextStep = nextStep;
        return this;
    }

    /**
     * Add a conditional transition to another step
     */
    public WorkflowStep<REQ, RESP> withConditionalNextStep(Predicate<RESP> condition, String nextStep) {
        conditionalNextSteps.put(condition, nextStep);
        return this;
    }

    /**
     * Execute this step
     * @return The name of the next step to execute
     */
    public String execute(ApiClient apiClient, ConfigProperties configProperties) throws ApiException, WorkflowException {
        log.info("Executing step: {}", name);

        RESP response = apiClient.execute(request, responseClass);

        log.debug("Response received for step: {}", name);

        // Process success handlers
        for (ResponseHandler<RESP> handler : successHandlers) {
            try {
                handler.handle(response, configProperties);
            } catch (JsonProcessingException e) {
                throw new WorkflowException("Error processing response in step: " + name, e);
            }
        }

        // Determine next step
        for (Map.Entry<Predicate<RESP>, String> entry : conditionalNextSteps.entrySet()) {
            if (entry.getKey().test(response)) {
                log.debug("Condition matched, next step: {}", entry.getValue());
                return entry.getValue();
            }
        }

        if (!Objects.equals(defaultNextStep,END)) {
            log.debug("Using default next step: {}", defaultNextStep);
            return defaultNextStep;
        }

        // If no next step defined, this is the end
        log.debug("No next step defined, workflow complete");
        return null;
    }
}
