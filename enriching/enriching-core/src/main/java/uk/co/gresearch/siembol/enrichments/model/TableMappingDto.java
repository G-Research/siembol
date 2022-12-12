package uk.co.gresearch.siembol.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing a tag used in an enrichment rule
 *
 * <p>This class is used for json (de)serialisation of a tag used in an enrichment rule and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 */
@Attributes(title = "table mapping", description = "Mapping definition for enriching an event")
public class TableMappingDto {
    @JsonProperty("table_name")
    @Attributes(required = true, description = "Name of table used for the enrichment")
    private String tableName;

    @JsonProperty("joining_key")
    @Attributes(required = true, description = "The key for joining the table with an event")
    private String joiningKey;

    @JsonProperty("tags")
    @Attributes(description = "Tags added after matching the joining key", minItems = 1)
    private List<TagDto> tags;

    @JsonProperty("enriching_fields")
    @Attributes(description = "Fields from the table added after matching the joining key", minItems = 1)
    private List<EnrichingFieldDto> enrichingFields;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getJoiningKey() {
        return joiningKey;
    }

    public void setJoiningKey(String joiningKey) {
        this.joiningKey = joiningKey;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public List<EnrichingFieldDto> getEnrichingFields() {
        return enrichingFields;
    }

    public void setEnrichingFields(List<EnrichingFieldDto> enrichingFields) {
        this.enrichingFields = enrichingFields;
    }
}
