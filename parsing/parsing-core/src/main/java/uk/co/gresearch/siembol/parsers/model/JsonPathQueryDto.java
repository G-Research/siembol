package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "json path query", description = "Specification for json path query")
public class JsonPathQueryDto {
    @JsonProperty("output_field")
    @Attributes(required = true, description = "Field for storing a query result")
    private String outputField;
    @JsonProperty("query")
    @Attributes(description = "Json Path query in a dot or bracket notation")
    private String query;

    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
