package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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
