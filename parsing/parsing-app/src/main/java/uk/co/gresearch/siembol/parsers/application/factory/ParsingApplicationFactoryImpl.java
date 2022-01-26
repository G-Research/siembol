package uk.co.gresearch.siembol.parsers.application.factory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.parsers.application.model.*;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationParser;
import uk.co.gresearch.siembol.parsers.application.parsing.RoutingParsingApplicationParser;
import uk.co.gresearch.siembol.parsers.application.parsing.SingleApplicationParser;
import uk.co.gresearch.siembol.parsers.application.parsing.SourceRoutingApplicationParser;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryResult;
import uk.co.gresearch.siembol.parsers.model.ParserConfigDto;
import uk.co.gresearch.siembol.parsers.model.ParsersConfigDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.OK;

public class ParsingApplicationFactoryImpl implements ParsingApplicationFactory {
    private static final String MISSING_PARSER_MSG = "Missing parser: %s in parser configurations";
    private static final String MISSING_SINGLE_PARSER = "Missing single_parser properties";
    private static final String MISSING_ROUTING_PARSER = "Missing routing_parser properties";
    private static final String MISSING_HEADER_ROUTING_PARSER = "Missing header_routing_parsing properties";
    private static final String MISSING_TOPIC_ROUTING_PARSER = "Missing topic_routing_parsing properties";

    private static final String UNSUPPORTED_PARSER_APP_TYPE = "Unsupported parsing application type %s";

    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(ParsersConfigDto.class);
    private static final ObjectWriter JSON_PARSER_CONFIG_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(ParserConfigDto.class);
    private static final ObjectReader JSON_PARSING_APP_READER = new ObjectMapper()
            .readerFor(ParsingApplicationDto.class);
    private static final ObjectWriter JSON_PARSING_APPS_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(ParsingApplicationsDto.class);

    private final JsonSchemaValidator jsonSchemaValidator;
    private final ParserFactory parserFactory;

    public ParsingApplicationFactoryImpl() throws Exception {
        jsonSchemaValidator = new SiembolJsonSchemaValidator(ParsingApplicationsDto.class);
        parserFactory = ParserFactoryImpl.createParserFactory();
    }

    @Override
    public ParsingApplicationFactoryResult getSchema() {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        attributes.setJsonSchema(jsonSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new ParsingApplicationFactoryResult(OK, attributes);
    }

    @Override
    public ParsingApplicationFactoryResult create(String parserApplicationConfig, String parserConfigs) {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        ParserFactoryResult parserConfigsResult = parserFactory.validateConfigurations(parserConfigs);
        if (parserConfigsResult.getStatusCode() != ParserFactoryResult.StatusCode.OK) {
            attributes.setMessage(parserConfigsResult.getAttributes().getMessage());
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }

        try {
            ParsingApplicationDto application = JSON_PARSING_APP_READER.readValue(parserApplicationConfig);
            attributes.setApplicationParserSpecification(parserApplicationConfig);
            addApplicationAttributes(attributes, application);
            attributes.setApplicationParser(createParser(application, parserConfigs));
        } catch (Exception e) {
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
        return new ParsingApplicationFactoryResult(OK, attributes);
    }

    @Override
    public ParsingApplicationFactoryResult create(String parserApplicationConfig) {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        try {
            ParsingApplicationDto application = JSON_PARSING_APP_READER.readValue(parserApplicationConfig);
            attributes.setApplicationParserSpecification(parserApplicationConfig);
            addApplicationAttributes(attributes, application);
        } catch (Exception e) {
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
        return new ParsingApplicationFactoryResult(OK, attributes);
    }

    @Override
    public ParsingApplicationFactoryResult validateConfiguration(String parserApplicationConfig) {
        try {
            String applications = wrapParserApplicationToParserApplications(parserApplicationConfig);
            return validateConfigurations(applications);
        } catch (IOException e) {
            ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
    }

    @Override
    public ParsingApplicationFactoryResult validateConfigurations(String parserConfigurations) {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        try {
            SiembolResult validationResult = jsonSchemaValidator.validate(parserConfigurations);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                attributes.setMessage(validationResult.getAttributes().getMessage());
                return new ParsingApplicationFactoryResult(ERROR, attributes);
            }

            return new ParsingApplicationFactoryResult(OK, attributes);
        } catch (Exception e) {
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
    }

    private ParsingApplicationParser createSingleParser(String applicationName,
                                                        Map<String, String> parsersMap,
                                                        ParsingSettingsDto parsingSettings,
                                                        ParsingApplicationSettingsDto appSettings) throws Exception {
        if (parsingSettings.getSingleParser() == null) {
            throw new IllegalArgumentException(MISSING_SINGLE_PARSER);
        }

        if (!parsersMap.containsKey(parsingSettings.getSingleParser().getParserName())) {
            String errorMsg = String.format(MISSING_PARSER_MSG,
                    parsingSettings.getSingleParser().getParserName());
            throw new IllegalArgumentException(errorMsg);
        }

        return SingleApplicationParser.builder()
                .parser(parsingSettings.getSingleParser().getOutputTopic(),
                        new SerializableSiembolParser(parsersMap
                                .get(parsingSettings.getSingleParser().getParserName())))
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .errorTopic(appSettings.getErrorTopic())
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName)
                .build();
    }

    private ParsingApplicationParser createRouterParser(String applicationName,
                                                        Map<String, String> parsersMap,
                                                        ParsingSettingsDto parsingSettings,
                                                        ParsingApplicationSettingsDto appSettings) throws Exception {
        if (parsingSettings.getRoutingParser() == null) {
            throw new IllegalArgumentException(MISSING_ROUTING_PARSER);
        }

        RoutingParserDto routingParser = parsingSettings.getRoutingParser();
        if (!parsersMap.containsKey(routingParser.getRouterParserName())) {
            String errorMsg = String.format(MISSING_PARSER_MSG, routingParser.getRouterParserName());
            throw new IllegalArgumentException(errorMsg);
        }
        if (!parsersMap.containsKey(routingParser.getDefaultParser().getParserName())) {
            String errorMsg = String.format(MISSING_PARSER_MSG, routingParser.getDefaultParser().getParserName());
            throw new IllegalArgumentException(errorMsg);
        }

        RoutingParsingApplicationParser.Builder<RoutingParsingApplicationParser> builder =
                RoutingParsingApplicationParser.builder()
                        .routerParser(new SerializableSiembolParser(parsersMap.get(routingParser.getRouterParserName())))
                        .defaultParser(routingParser.getDefaultParser().getOutputTopic(),
                                new SerializableSiembolParser(
                                        parsersMap.get(routingParser.getDefaultParser().getParserName())))
                        .routingConditionField(routingParser.getRoutingField())
                        .routingMessageField(routingParser.getRoutingMessage())
                        .mergedFields(routingParser.getMergedFields());

        for (RoutedParserPropertiesDto routedParser : routingParser.getParsers()) {
            if (!parsersMap.containsKey(routedParser.getParserProperties().getParserName())) {
                String errorMsg = String.format(MISSING_PARSER_MSG, routedParser.getParserProperties().getParserName());
                throw new IllegalArgumentException(errorMsg);
            }
            builder.addParser(routedParser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(parsersMap.get(routedParser.getParserProperties().getParserName())),
                    routedParser.getRoutingFieldPattern());
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName);

        return builder.build();
    }

    private ParsingApplicationParser createHeaderRouterParser(String applicationName,
                                                              Map<String, String> parsersMap,
                                                              ParsingSettingsDto parsingSettings,
                                                              ParsingApplicationSettingsDto appSettings) throws Exception {
        var headerRoutingParser = parsingSettings.getHeaderRoutingParserDto();
        if (headerRoutingParser == null) {
            throw new IllegalArgumentException(MISSING_HEADER_ROUTING_PARSER);
        }

        if (!parsersMap.containsKey(headerRoutingParser.getDefaultParser().getParserName())) {
            String errorMsg = String.format(MISSING_PARSER_MSG, headerRoutingParser.getDefaultParser().getParserName());
            throw new IllegalArgumentException(errorMsg);
        }

        var builder = SourceRoutingApplicationParser.builder()
                .defaultParser(headerRoutingParser.getDefaultParser().getOutputTopic(),
                        new SerializableSiembolParser(
                                parsersMap.get(headerRoutingParser.getDefaultParser().getParserName())));

        for (var parser : headerRoutingParser.getParsers()) {
            if (!parsersMap.containsKey(parser.getParserProperties().getParserName())) {
                String errorMsg = String.format(MISSING_PARSER_MSG, parser.getParserProperties().getParserName());
                throw new IllegalArgumentException(errorMsg);
            }
            builder.addParser(parser.getSourceHeaderValue(),
                    parser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(parsersMap.get(parser.getParserProperties().getParserName())));
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName);

        return builder.build();
    }

    private ParsingApplicationParser createTopicRouterParser(String applicationName,
                                                             Map<String, String> parsersMap,
                                                             ParsingSettingsDto parsingSettings,
                                                             ParsingApplicationSettingsDto appSettings) throws Exception {
        var topicRoutingParser = parsingSettings.getTopicRoutingParserDto();
        if (topicRoutingParser == null) {
            throw new IllegalArgumentException(MISSING_TOPIC_ROUTING_PARSER);
        }

        if (!parsersMap.containsKey(topicRoutingParser.getDefaultParser().getParserName())) {
            String errorMsg = String.format(MISSING_PARSER_MSG, topicRoutingParser.getDefaultParser().getParserName());
            throw new IllegalArgumentException(errorMsg);
        }

        var builder = SourceRoutingApplicationParser.builder()
                .defaultParser(topicRoutingParser.getDefaultParser().getOutputTopic(),
                        new SerializableSiembolParser(
                                parsersMap.get(topicRoutingParser.getDefaultParser().getParserName())));

        for (var parser : topicRoutingParser.getParsers()) {
            if (!parsersMap.containsKey(parser.getParserProperties().getParserName())) {
                String errorMsg = String.format(MISSING_PARSER_MSG, parser.getParserProperties().getParserName());
                throw new IllegalArgumentException(errorMsg);
            }
            builder.addParser(parser.getTopicName(),
                    parser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(parsersMap.get(parser.getParserProperties().getParserName())));
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName);

        return builder.build();
    }

    private ParsingApplicationParser createParser(ParsingApplicationDto application,
                                                  String parserConfigs) throws Exception {
        ParsersConfigDto parsers = JSON_PARSERS_CONFIG_READER.readValue(parserConfigs);
        Map<String, String> parsersMap = parsers.getParserConfigurations().stream()
                .collect(Collectors.toMap(ParserConfigDto::getParserName, x -> {
                    try {
                        return JSON_PARSER_CONFIG_WRITER.writeValueAsString(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }));

        var appSettings = application.getParsingApplicationSettingsDto();
        var parsingSettings = application.getParsingSettingsDto();
        switch (appSettings.getApplicationType()) {
            case SINGLE_PARSER:
                return createSingleParser(application.getParsingApplicationName(), parsersMap, parsingSettings, appSettings);
            case ROUTER_PARSING:
                return createRouterParser(application.getParsingApplicationName(), parsersMap, parsingSettings, appSettings);
            case TOPIC_ROUTING_PARSING:
                return createTopicRouterParser(application.getParsingApplicationName(), parsersMap, parsingSettings, appSettings);
            case HEADER_ROUTING_PARSING:
                return createHeaderRouterParser(application.getParsingApplicationName(), parsersMap, parsingSettings, appSettings);
            default:
                throw new IllegalArgumentException(String.format(UNSUPPORTED_PARSER_APP_TYPE, appSettings.getApplicationType()));
        }
    }

    private String wrapParserApplicationToParserApplications(String configStr) throws IOException {
        ParsingApplicationDto application = JSON_PARSING_APP_READER.readValue(configStr);
        ParsingApplicationsDto applications = new ParsingApplicationsDto();
        applications.setParsingApplicationsVersion(application.getParsingApplicationVersion());

        applications.setParsingApplications(List.of(application));
        return JSON_PARSING_APPS_WRITER.writeValueAsString(applications);
    }

    private void addApplicationAttributes(ParsingApplicationFactoryAttributes attributes,
                                          ParsingApplicationDto application) {
        attributes.setName(application.getParsingApplicationName());
        attributes.setInputParallelism(application.getParsingApplicationSettingsDto().getInputParallelism());
        attributes.setOutputParallelism(application.getParsingApplicationSettingsDto().getOutputParallelism());
        attributes.setParsingParallelism(application.getParsingApplicationSettingsDto().getParsingParallelism());
        attributes.setInputTopics(application.getParsingApplicationSettingsDto().getInputTopics());
        attributes.setApplicationType(application.getParsingApplicationSettingsDto().getApplicationType());
        if (ParsingApplicationTypeDto.HEADER_ROUTING_PARSING.equals(
                application.getParsingApplicationSettingsDto().getApplicationType())) {
            attributes.setSourceHeaderName(
                    application.getParsingSettingsDto().getHeaderRoutingParserDto().getHeaderName());
        }
    }
}
