package uk.co.gresearch.siembol.parsers.model;

import com.github.reinert.jjschema.Attributes;

import java.util.List;

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
