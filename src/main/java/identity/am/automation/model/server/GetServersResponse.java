package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for the get servers request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetServersResponse extends ApiResponse {

    @JsonProperty("result")
    private List<ServerInfo> result;

    @JsonProperty("resultCount")
    private int resultCount;

    @JsonProperty("pagedResultsCookie")
    private String pagedResultsCookie;

    @JsonProperty("totalPagedResultsPolicy")
    private String totalPagedResultsPolicy;

    @JsonProperty("totalPagedResults")
    private int totalPagedResults;

    @JsonProperty("remainingPagedResults")
    private int remainingPagedResults;

    /**
     * Find a server by its URL
     * @param url The URL to match
     * @return The server with the matching URL or null if not found
     */
    public ServerInfo findServerByUrl(String url) {
        if (result == null || result.isEmpty()) {
            return null;
        }

        return result.stream()
                .filter(server -> url.equals(server.getUrl()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isValid() {
        return result != null && !result.isEmpty();
    }
}