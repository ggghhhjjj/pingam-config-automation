package com.pingidentity.pingam.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response from listing realms
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ListRealmsResponse extends ApiResponse {

    @JsonProperty("result")
    private List<Map<String, Object>> result;

    @JsonProperty("resultCount")
    private Integer resultCount;

    @JsonProperty("pagedResultsCookie")
    private String pagedResultsCookie;

    @JsonProperty("totalPagedResultsPolicy")
    private String totalPagedResultsPolicy;

    @JsonProperty("totalPagedResults")
    private Integer totalPagedResults;

    @JsonProperty("remainingPagedResults")
    private Integer remainingPagedResults;

    @Override
    public boolean isValid() {
        return result != null;
    }
}
