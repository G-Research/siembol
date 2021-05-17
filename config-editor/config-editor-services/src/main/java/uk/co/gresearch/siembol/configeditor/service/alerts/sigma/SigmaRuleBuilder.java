package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.databind.JsonNode;

public class SigmaRuleBuilder {
    //search types
    // list search type - field == original_string by default
    //
    // map search
    private SigmaImporterAttributesDto importerAttributesDto;
    private SigmaRuleDto sigmaRuleDto;
    private JsonNode detection;
}
