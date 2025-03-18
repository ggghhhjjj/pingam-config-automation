package identity.am.automation.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for the get realms request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetRealmsResponse extends ApiResponse {

    @JsonProperty("result")
    private List<RealmInfo> result;

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
     * Find the top level realm (with null parentPath)
     * @return The top level realm or null if not found
     */
    public RealmInfo getTopLevelRealm() {
        if (result == null || result.isEmpty()) {
            return null;
        }

        return result.stream()
                .filter(realm -> realm.getParentPath() == null)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isValid() {
        return result != null && getTopLevelRealm() != null;
    }
}
