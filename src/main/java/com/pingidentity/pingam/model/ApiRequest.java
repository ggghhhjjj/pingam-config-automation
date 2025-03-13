package com.pingidentity.pingam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all API requests
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ApiRequest {
    @JsonIgnore
    private String endpoint;

    @JsonIgnore
    private HttpMethod method;

    @JsonIgnore
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    @JsonIgnore
    @Builder.Default
    private Map<String, String> queryParams = new HashMap<>();

    /**
     * Get the request body for serialization
     * Default implementation returns this, subclasses can override to provide custom body
     */
    @JsonIgnore
    public Object getBody() {
        return this;
    }

    /**
     * Add a header to the request
     */
    public ApiRequest withHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Add a query parameter to the request
     */
    public ApiRequest withQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }
}