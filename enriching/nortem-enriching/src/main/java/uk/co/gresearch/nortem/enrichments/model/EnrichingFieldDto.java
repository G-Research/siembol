package uk.co.gresearch.nortem.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "enriching field", description = "Mapping definition for enriching field")
public class EnrichingFieldDto {
    @JsonProperty("table_field_name")
    @Attributes(required = true, description = "The name of the field in the table")
    private String tableFieldName;
    @JsonProperty("event_field_name")
    @Attributes(required = true, description = "The name of the field added into the event after enriching")
    private String eventFieldName;

    public String getTableFieldName() {
        return tableFieldName;
    }

    public void setTableFieldName(String tableFieldName) {
        this.tableFieldName = tableFieldName;
    }

    public String getEventFieldName() {
        return eventFieldName;
    }

    public void setEventFieldName(String eventFieldName) {
        this.eventFieldName = eventFieldName;
    }
}
