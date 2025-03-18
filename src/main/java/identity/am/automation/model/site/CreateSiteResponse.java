package identity.am.automation.model.site;

import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from site creation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateSiteResponse extends ApiResponse {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("id")
    private String numericId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("secondaryURLs")
    private List<String> secondaryURLs;

    @JsonProperty("servers")
    private List<String> servers;

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty() && url != null && !url.isEmpty();
    }
}
