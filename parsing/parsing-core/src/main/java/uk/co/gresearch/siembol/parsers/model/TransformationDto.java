package uk.co.gresearch.siembol.parsers.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes for a transformation configuration
 *
 * <p>This class is used for json (de)serialisation of a transformation configuration and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see TransformationTypeDto
 * @see TransformationAttributesDto
 */
@Attributes(title = "transformation", description = "The specification of transformation")
public class TransformationDto {
    @Attributes(description = "The transformation is enabled", required = false)
    @JsonProperty("is_enabled")
    private boolean enabled = true;

    @Attributes(description = "Description of the transformation", required = false)
    @JsonProperty("description")
    private String description;
    @JsonProperty("transformation_type")
    @Attributes(required = true, description = "The type of the transformation")
    private TransformationTypeDto type;
    @Attributes(required = true, description = "The attributes of the transformation")
    private TransformationAttributesDto attributes;

    public TransformationTypeDto getType() {
        return type;
    }

    public void setType(TransformationTypeDto type) {
        this.type = type;
    }

    public TransformationAttributesDto getAttributes() {
        return attributes;
    }

    public void setAttributes(TransformationAttributesDto attributes) {
        this.attributes = attributes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
