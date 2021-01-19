package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.configinfo.JsonRuleConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.alerts.AlertingRuleSchemaService;
import uk.co.gresearch.siembol.configeditor.service.enrichments.EnrichmentSchemaService;
import uk.co.gresearch.siembol.configeditor.service.parserconfig.ParserConfigConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.service.parserconfig.ParserConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.service.parsingapp.ParsingAppConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.service.parsingapp.ParsingAppConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.service.response.ResponseSchemaService;

import java.util.Map;
import java.util.Optional;

public enum ConfigEditorServiceType implements ConfigSchemaServiceFactory {
    RESPONSE("response", JsonRuleConfigInfoProvider.create(),
            (x, y) -> ResponseSchemaService.createResponseSchemaService(x, y)),
    ALERT("alert", JsonRuleConfigInfoProvider.create(),
            (x, y) -> AlertingRuleSchemaService.createAlertingRuleSchemaService(x)),
    CORRELATION_ALERT("correlationalert", JsonRuleConfigInfoProvider.create(),
            (x, y) -> AlertingRuleSchemaService.createAlertingCorrelationRuleSchemaService(x)),
    PARSER_CONFIG("parserconfig", ParserConfigConfigInfoProvider.create(),
            (x, y) -> ParserConfigSchemaService.createParserConfigSchemaService(x)),
    PARSING_APP("parsingapp", ParsingAppConfigInfoProvider.create(),
            (x, y) -> ParsingAppConfigSchemaService.createParsingAppConfigSchemaService(x)),
    ENRICHMENT("enrichment", JsonRuleConfigInfoProvider.create(),
            (x, y) -> EnrichmentSchemaService.createEnrichmentsSchemaService(x));

    private static final String UNSUPPORTED_SERVICE_NAME = "Unsupported service name";
    private final String name;
    private final ConfigInfoProvider configInfoProvider;
    private final ConfigSchemaServiceFactory configSchemaServiceFactory;

    ConfigEditorServiceType(
            String name,
            ConfigInfoProvider configInfoProvider,
            ConfigSchemaServiceFactory configSchemaServiceFactory) {
        this.name = name;
        this.configInfoProvider = configInfoProvider;
        this.configSchemaServiceFactory = configSchemaServiceFactory;
    }

    @Override
    public ConfigSchemaService createConfigSchemaService(
            ConfigEditorUiLayout uiLayout,
            Optional<Map<String, String>> attributes) throws Exception {
        return configSchemaServiceFactory.createConfigSchemaService(uiLayout, attributes);
    }

    public String getName() {
        return name;
    }

    public static ConfigEditorServiceType fromName(String name) {
        for (ConfigEditorServiceType serviceType : ConfigEditorServiceType.values()) {
            if (serviceType.getName().equalsIgnoreCase(name)) {
                return serviceType;
            }
        }

        throw new IllegalArgumentException(UNSUPPORTED_SERVICE_NAME);
    }

    public ConfigInfoProvider getConfigInfoProvider() {
        return configInfoProvider;
    }
}