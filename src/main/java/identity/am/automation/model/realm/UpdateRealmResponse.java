package identity.am.automation.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;
import identity.am.automation.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for the update realm request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRealmResponse extends ApiResponse {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("name")
    private String name;

    @JsonProperty("aliases")
    private List<String> aliases;

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty() && aliases != null;
    }
}
