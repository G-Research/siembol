package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.configeditor.configinfo.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.common.constants.ServiceType;
import uk.co.gresearch.siembol.configeditor.configinfo.JsonRuleConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.model.AdditionalConfigTesters;
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

public enum ConfigEditorServiceFactory implements ConfigSchemaServiceFactory {
    RESPONSE_FACTORY(ServiceType.RESPONSE, JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> ResponseSchemaService.createResponseSchemaService(x, y)),
    ALERT_FACTORY(ServiceType.ALERT, JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> AlertingRuleSchemaService.createAlertingRuleSchemaService(x, z)),
    CORRELATION_ALERT_FACTORY(ServiceType.CORRELATION_ALERT, JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> AlertingRuleSchemaService.createAlertingCorrelationRuleSchemaService(x)),
    PARSER_CONFIG_FACTORY(ServiceType.PARSER_CONFIG, ParserConfigConfigInfoProvider.create(),
            (x, y, z) -> ParserConfigSchemaService.createParserConfigSchemaService(x)),
    PARSING_APP_FACTORY(ServiceType.PARSING_APP, ParsingAppConfigInfoProvider.create(),
            (x, y, z) -> ParsingAppConfigSchemaService.createParsingAppConfigSchemaService(x)),
    ENRICHMENT_FACTORY(ServiceType.ENRICHMENT, JsonRuleConfigInfoProvider.create(),
            (x, y, z) -> EnrichmentSchemaService.createEnrichmentsSchemaService(x));

    private static final String UNSUPPORTED_SERVICE_TYPE = "Unsupported service type";
    private final ServiceType serviceType;
    private final ConfigInfoProvider configInfoProvider;
    private final ConfigSchemaServiceFactory configSchemaServiceFactory;

    ConfigEditorServiceFactory(
            ServiceType serviceType,
            ConfigInfoProvider configInfoProvider,
            ConfigSchemaServiceFactory configSchemaServiceFactory) {
        this.serviceType = serviceType;
        this.configInfoProvider = configInfoProvider;
        this.configSchemaServiceFactory = configSchemaServiceFactory;
    }

    @Override
    public ConfigSchemaService createConfigSchemaService(
            ConfigEditorUiLayout uiLayout,
            Optional<Map<String, String>> attributes,
            Optional<AdditionalConfigTesters> additionalTesters) throws Exception {
        return configSchemaServiceFactory.createConfigSchemaService(uiLayout,
                attributes,
                additionalTesters);
    }

    public String getName() {
        return serviceType.getName();
    }

    public static ConfigEditorServiceFactory fromServiceType(ServiceType serviceType) {
        for (ConfigEditorServiceFactory factory : ConfigEditorServiceFactory.values()) {
            if (factory.serviceType.equals(serviceType)) {
                return factory;
            }
        }

        throw new IllegalArgumentException(UNSUPPORTED_SERVICE_TYPE);
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public ConfigInfoProvider getConfigInfoProvider() {
        return configInfoProvider;
    }
}