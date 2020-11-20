package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.configinfo.JsonRuleConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.service.alerts.AlertingRuleSchemaServiceImpl;
import uk.co.gresearch.siembol.configeditor.service.enrichments.EnrichmentSchemaServiceImpl;
import uk.co.gresearch.siembol.configeditor.service.parserconfig.ParserConfigConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.service.parserconfig.ParserConfigSchemaServiceImpl;
import uk.co.gresearch.siembol.configeditor.service.parsingapp.ParsingAppConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.service.parsingapp.ParsingAppConfigSchemaServiceImpl;
import uk.co.gresearch.siembol.configeditor.service.response.ResponseSchemaService;

import java.util.Map;
import java.util.Optional;

public enum ConfigEditorServiceType implements ConfigSchemaServiceFactory {
    RESPONSE("response", JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> ResponseSchemaService.createResponseSchemaService(x, z)),
    ALERT("alert", JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> AlertingRuleSchemaServiceImpl.createAlertingRuleSchemaService(x, y)),
    CORRELATION_ALERT("correlationalert", JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> AlertingRuleSchemaServiceImpl.createAlertingCorrelationRuleSchemaService(x)),
    PARSER_CONFIG("parserconfig", ParserConfigConfigInfoProvider.create(),
            (x, y, z) -> ParserConfigSchemaServiceImpl.createParserConfigSchemaService(x, y)),
    PARSING_APP("parsingapp", ParsingAppConfigInfoProvider.create(),
            (x, y, z) -> ParsingAppConfigSchemaServiceImpl.createParsingAppConfigSchemaService(x)),
    ENRICHMENT("enrichment", JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> EnrichmentSchemaServiceImpl.createEnrichmentsSchemaService(x, y));

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
            Optional<String> uiLayoutConfig,
            Optional<String> uiTestLayoutConfig,
            Optional<Map<String, String>> attributes) throws Exception {
        return configSchemaServiceFactory.createConfigSchemaService(uiLayoutConfig, uiTestLayoutConfig, attributes);
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