package com.pingidentity.pingam.workflow;

import com.pingidentity.pingam.config.ConfigProperties;
import com.pingidentity.pingam.model.ApiResponse;

/**
 * Handler interface for processing responses
 */
@FunctionalInterface
public interface ResponseHandler<RESP extends ApiResponse> {
    void handle(RESP response, ConfigProperties configProperties);
}
