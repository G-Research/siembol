package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes for field rename transformation
 *
 * <p>This class is used for json (de)serialisation of field rename transformation attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "field rename", description = "Specification for renaming fields")
public class FieldRenameDto {
    @JsonProperty("field_to_rename")
    @Attributes(required = true, description = "Field name that should be renamed")
    private String fieldToRename;
    @JsonProperty("new_name")
    @Attributes(required = true, description = "New field name after renaming")
    private String newName;

    public String getFieldToRename() {
        return fieldToRename;
    }

    public void setFieldToRename(String fieldToRename) {
        this.fieldToRename = fieldToRename;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
