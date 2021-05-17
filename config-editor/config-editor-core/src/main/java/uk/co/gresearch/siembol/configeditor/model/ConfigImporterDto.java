package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigImporterDto {
    @JsonProperty("importer_name")
    private String importerName;
    @JsonProperty("importer_attributes_schema")
    private String importerAttributesSchema;

    public String getImporterName() {
        return importerName;
    }

    public void setImporterName(String importerName) {
        this.importerName = importerName;
    }

    public String getImporterAttributesSchema() {
        return importerAttributesSchema;
    }

    public void setImporterAttributesSchema(String importerAttributesSchema) {
        this.importerAttributesSchema = importerAttributesSchema;
    }
}
