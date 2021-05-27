package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

import com.github.reinert.jjschema.Attributes;
import com.fasterxml.jackson.annotation.JsonProperty;

@Attributes(title = "sigma field mapping item", description = "Sigma to siembol field mapping item")
public class SigmaFieldMappingItemDto {
    public SigmaFieldMappingItemDto() {
    }

    public SigmaFieldMappingItemDto(String sigmaField, String siembolField) {
        this.sigmaField = sigmaField;
        this.siembolField = siembolField;
    }

    @JsonProperty("sigma_field")
    @Attributes(required = true, description = "Sigma field that will be renamed in the rule to import")
    private String sigmaField;
    @JsonProperty("siembol_field")
    @Attributes(required = true, description = "Siembol field that will be used in the imported rule")
    private String siembolField;

    public String getSigmaField() {
        return sigmaField;
    }

    public void setSigmaField(String sigmaField) {
        this.sigmaField = sigmaField;
    }

    public String getSiembolField() {
        return siembolField;
    }

    public void setSiembolField(String siembolField) {
        this.siembolField = siembolField;
    }
}
