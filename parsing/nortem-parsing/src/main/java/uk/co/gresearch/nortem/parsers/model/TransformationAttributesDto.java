package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "transformation attributes", description = "The attributes for the transformation function")
public class TransformationAttributesDto {
    @JsonProperty("string_replace_target")
    @Attributes(description = "target that will be replaced in JAVA String replace function")
    private String stringReplaceTarget;
    @JsonProperty("string_replace_replacement")
    @Attributes(description = "Replacement will replace target in JAVA String replace function")
    private String stringReplaceReplacement;

    @JsonProperty("field_rename_map")
    @Attributes(description = "Mapping for field rename transformation", minItems = 1)
    private List<FieldRenameDto> fieldRenameMap;

    @JsonProperty("fields_filter")
    @Attributes(description = "Filter for selecting the fields for the transformation")
    private FieldsFilterDto fieldsFilter;

    @JsonProperty("message_filter")
    @Attributes(description = "Filter for filtering the whole message")
    private MessageFilterDto messageFilter;

    public String getStringReplaceTarget() {
        return stringReplaceTarget;
    }

    public void setStringReplaceTarget(String stringReplaceTarget) {
        this.stringReplaceTarget = stringReplaceTarget;
    }

    public String getStringReplaceReplacement() {
        return stringReplaceReplacement;
    }

    public void setStringReplaceReplacement(String stringReplaceReplacement) {
        this.stringReplaceReplacement = stringReplaceReplacement;
    }

    public List<FieldRenameDto> getFieldRenameMap() {
        return fieldRenameMap;
    }

    public void setFieldRenameMap(List<FieldRenameDto> fieldRenameMap) {
        this.fieldRenameMap = fieldRenameMap;
    }

    public FieldsFilterDto getFieldsFilter() {
        return fieldsFilter;
    }

    public void setFieldsFilter(FieldsFilterDto fieldsFilter) {
        this.fieldsFilter = fieldsFilter;
    }

    public MessageFilterDto getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(MessageFilterDto messageFilter) {
        this.messageFilter = messageFilter;
    }
}
