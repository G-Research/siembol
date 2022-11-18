package uk.co.gresearch.siembol.parsers.model;

import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing attributes for a message filter transformation
 *
 * <p>This class is used for json (de)serialisation of a message filter transformation and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see MessageFilterMatcherDto
 */
@Attributes(title = "message filter", description = "Specification for message filter")
public class MessageFilterDto {
    @Attributes(required = true, description = "Matchers for the message filter", minItems = 1)
    private List<MessageFilterMatcherDto> matchers;

    public List<MessageFilterMatcherDto> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<MessageFilterMatcherDto> matchers) {
        this.matchers = matchers;
    }
}
