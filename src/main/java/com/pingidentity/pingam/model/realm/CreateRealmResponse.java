package com.pingidentity.pingam.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pingidentity.pingam.model.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from realm creation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateRealmResponse extends ApiResponse {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String revision;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("aliases")
    private List<String> aliases;

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty() && active != null && active;
    }
}
