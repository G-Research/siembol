package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.github.reinert.jjschema.Attributes;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;

@Attributes(title = "sigma importer attributes", description = "Attributes for importing a sigma rule into siembol")
public class SigmaImporterAttributesDto {
    @JsonProperty("field_mapping")
    @Attributes(description = "Sigma event fields that will be renamed in the rule to import", minItems = 1)
    private List<SigmaFieldMappingItemDto> fieldMapping;

    @JsonProperty("rule_metadata_mapping")
    @Attributes(required = true, description = "Sigma metadata fields that will be renamed in the rule to import", minItems = 1)
    private List<SigmaFieldMappingItemDto> ruleMetadataMapping;

    @JsonProperty("source_types")
    @Attributes(required = true, description = "Source types for the rule, use '*' for all source types", minItems = 1)
    private List<String> sourceTypes;

    public SigmaImporterAttributesDto() {
        ruleMetadataMapping = Arrays.asList(new SigmaFieldMappingItemDto[]{
                new SigmaFieldMappingItemDto("id", "sigma_id"),
                new SigmaFieldMappingItemDto("tags", "sigma_tags"),
                new SigmaFieldMappingItemDto("title", "rule_name"),
                new SigmaFieldMappingItemDto("falsepositives", "sigma_fp"),
        });
    }

    public List<SigmaFieldMappingItemDto> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(List<SigmaFieldMappingItemDto> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public List<SigmaFieldMappingItemDto> getRuleMetadataMapping() {
        return ruleMetadataMapping;
    }

    public void setRuleMetadataMapping(List<SigmaFieldMappingItemDto> ruleMetadataMapping) {
        this.ruleMetadataMapping = ruleMetadataMapping;
    }

    public List<String> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(List<String> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }
}
