package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
/**
 * A data transfer object that represents config importer attributes
 *
 * <p>This class represents config importer attributes such as the name and its attributes json schema.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 * @see JsonRawValue
 */
public class ConfigImporterDto {
    @JsonProperty("importer_name")
    private String importerName;
    @JsonRawValue
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
