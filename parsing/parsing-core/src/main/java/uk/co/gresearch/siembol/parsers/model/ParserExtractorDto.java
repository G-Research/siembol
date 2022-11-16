package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing attributes for a parser extractor
 *
 * <p>This class is used for json (de)serialisation of a parser extractor and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see PreProcessingFunctionDto
 * @see ExtractorTypeDto
 * @see ExtractorAttributesDto
 */
@Attributes(title = "parser extractor", description = "Parser extractor specification")
public class ParserExtractorDto {
    @Attributes(description = "The extractor is enabled", required = false)
    @JsonProperty("is_enabled")
    private boolean enabled = true;

    @Attributes(description = "Description of the extractor", required = false)
    @JsonProperty("description")
    private String description;
    @Attributes(required = true, description = "The name of the extractor")
    private String name;

    @Attributes(required = true, description = "The field on which the extractor is applied")
    private String field;

    @JsonProperty("pre_processing_function")
    @Attributes(description = "The pre-processing function applied before the extractor")
    private PreProcessingFunctionDto preProcessingFunction;

    @JsonProperty("post_processing_functions")
    @Attributes(description = "The postprocessing function applied after the extractor", minItems = 1)
    private List<PostProcessingFunctionDto> postProcessingFunctions;

    @Attributes(required = true, description = "The extractor type")
    @JsonProperty("extractor_type")
    private ExtractorTypeDto type;

    @Attributes(required = true, description = "The attributes of the extractor and related functions")
    ExtractorAttributesDto attributes;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ExtractorTypeDto getType() {
        return type;
    }

    public void setType(ExtractorTypeDto type) {
        this.type = type;
    }

    public ExtractorAttributesDto getAttributes() {
        return attributes;
    }

    public void setAttributes(ExtractorAttributesDto attributes) {
        this.attributes = attributes;
    }

    public List<PostProcessingFunctionDto> getPostProcessingFunctions() {
        return postProcessingFunctions;
    }

    public void setPostProcessingFunctions(List<PostProcessingFunctionDto> postProcessingFunctions) {
        this.postProcessingFunctions = postProcessingFunctions;
    }

    public PreProcessingFunctionDto getPreProcessingFunction() {
        return preProcessingFunction;
    }

    public void setPreProcessingFunction(PreProcessingFunctionDto preProcessingFunction) {
        this.preProcessingFunction = preProcessingFunction;
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
