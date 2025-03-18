package identity.am.automation.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import identity.am.automation.config.ConfigProperties;
import identity.am.automation.model.ApiResponse;

/**
 * Handler interface for processing responses
 */
@FunctionalInterface
public interface ResponseHandler<RESP extends ApiResponse> {
    void handle(RESP response, ConfigProperties configProperties) throws JsonProcessingException;
}
