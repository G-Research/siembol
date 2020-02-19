package uk.co.gresearch.nortem.parsers.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "transformation", description = "The specification of transformation")
public class TransformationDto {
    @JsonProperty("transformation_type")
    @Attributes(required = true, description = "The type of the transformation")
    private TransformationTypeDto type;
    @Attributes(description = "The attributes of the transformation")
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
}
