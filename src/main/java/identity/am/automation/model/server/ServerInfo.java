package identity.am.automation.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Individual server information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerInfo {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("siteName")
    private String siteName;
}