package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

import com.github.reinert.jjschema.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.siembol.alerts.model.TagDto;

import java.util.Arrays;
import java.util.List;

@Attributes(title = "sigma importer attributes", description = "Attributes for importing a sigma rule into siembol")
public class SigmaImporterAttributesDto {
    @JsonProperty("field_mapping")
    @Attributes(description = "Sigma event fields that will be renamed in the rule to import", minItems = 1)
    private List<SigmaFieldMappingItemDto> fieldMapping;

    @JsonProperty("rule_metadata_mapping")
    @Attributes(required = true, description = "Sigma metadata fields that will be renamed in the rule to import")
    private SigmaRuleMetadataMappingDto ruleMetadataMapping = new SigmaRuleMetadataMappingDto();

    public SigmaImporterAttributesDto() {
        new TagDto();
        ruleMetadataMapping.setTags(Arrays.asList(TagDto.from("sigma_id", "${id}"),
                TagDto.from("sigma_tags", "${tags}"),
                TagDto.from("sigma_fp", "${falsepositives}")));
    }

    public List<SigmaFieldMappingItemDto> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(List<SigmaFieldMappingItemDto> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public SigmaRuleMetadataMappingDto getRuleMetadataMapping() {
        return ruleMetadataMapping;
    }

    public void setRuleMetadataMapping(SigmaRuleMetadataMappingDto ruleMetadataMapping) {
        this.ruleMetadataMapping = ruleMetadataMapping;
    }
}
