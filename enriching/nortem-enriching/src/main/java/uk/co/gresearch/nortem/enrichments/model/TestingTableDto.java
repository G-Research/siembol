package uk.co.gresearch.nortem.enrichments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

import java.util.List;

@Attributes(title = "enrichments test specification", description = "The specification for testing enichments")
public class TestingTableDto {
    @JsonProperty("table_name")
    @Attributes(required = true, description = "Mapping for a testing table")
    private String tableName;

    @JsonProperty("table_mapping")
    @Attributes(required = true, description = "Mapping for a testing table")
    private JsonRawStringDto tableMapping;

    @JsonIgnore
    @SchemaIgnore
    private String tableMappingContent;

    @JsonSetter
    public void tableMapping(JsonNode tableMapping) {
        this.tableMappingContent = tableMapping.toString();
    }

    @JsonIgnore
    public String getTableMappingContent() {
        return tableMappingContent;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
