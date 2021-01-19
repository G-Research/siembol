package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "admin config", description = "Generic Attributes for admin configuration")
public class AdminConfigDto {
    @JsonProperty("config_version")
    @Attributes(required = true, description = "The version of the admin configuration", minimum = 1)
    private Integer configVersion = 1;

    public Integer getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }
}
