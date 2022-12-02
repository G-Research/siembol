package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing a generic admin configuration
 *
 * <p>This class is used for json (de)serialisation a generic admin configuration and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "admin config", description = "Generic Attributes for admin configuration")
public class AdminConfigDto {
    @JsonProperty("config_version")
    @Attributes(required = true, description = "The version of the admin configuration", minimum = 0)
    private Integer configVersion = 0;

    public Integer getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

}
