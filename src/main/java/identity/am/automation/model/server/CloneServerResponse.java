package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response from server cloning
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CloneServerResponse extends ApiResponse {

    @JsonProperty("clonedId")
    private String clonedId;

    @JsonProperty("clonedUrl")
    private String clonedUrl;

    @Override
    public boolean isValid() {
        return clonedId != null && !clonedId.isEmpty() && clonedUrl != null && !clonedUrl.isEmpty();
    }
}