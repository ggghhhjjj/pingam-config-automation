package identity.am.automation.model.realm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Individual realm information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RealmInfo {

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
}
