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
import uk.co.gresearch.siembol.parsers.model.ParserAttributesDto;
import uk.co.gresearch.siembol.parsers.model.ParserConfigDto;
import uk.co.gresearch.siembol.parsers.model.ParserTypeDto;
import uk.co.gresearch.siembol.parsers.model.ParsersConfigDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.OK;
/**
 * An object for compiling parsing applications
 *
 * <p>This class in an implementation of ParsingApplicationFactory interface.
 * It is used for creating a parsing applications, validating parsing application configuration and
 * providing json schema for parsing application configurations.
 *
 * @author  Marian Novotny
 * @see ParsingApplicationFactory
 *
 */
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

    private static final ObjectReader JSON_PARSING_APPS_READER = new ObjectMapper()
            .readerFor(ParsingApplicationsDto.class);
    private static final ObjectWriter JSON_PARSING_APPS_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(ParsingApplicationsDto.class);

    private final JsonSchemaValidator jsonSchemaValidator;
    private final ParserFactory parserFactory;
    private final Function<String, String> getParserDummyFun;

    /**
     * Creates ParsingApplicationFactoryImpl instance
     *
     * @throws Exception if the creation fails
     */
    public ParsingApplicationFactoryImpl() throws Exception {
        jsonSchemaValidator = new SiembolJsonSchemaValidator(ParsingApplicationsDto.class);
        parserFactory = ParserFactoryImpl.createParserFactory();
        final var dummyParser = createDummyParser();
        getParserDummyFun = x -> dummyParser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParsingApplicationFactoryResult getSchema() {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        attributes.setJsonSchema(jsonSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new ParsingApplicationFactoryResult(OK, attributes);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ParsingApplicationFactoryResult validateConfigurations(String parserApplicationConfigurations) {
        ParsingApplicationFactoryAttributes attributes = new ParsingApplicationFactoryAttributes();
        try {
            SiembolResult validationResult = jsonSchemaValidator.validate(parserApplicationConfigurations);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                attributes.setMessage(validationResult.getAttributes().getMessage());
                return new ParsingApplicationFactoryResult(ERROR, attributes);
            }

            ParsingApplicationsDto applications = JSON_PARSING_APPS_READER.readValue(parserApplicationConfigurations);
            for (var application: applications.getParsingApplications()) {
                createParser(application, getParserDummyFun);
            }

            return new ParsingApplicationFactoryResult(OK, attributes);
        } catch (Exception e) {
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
    }

    private ParsingApplicationParser createSingleParser(String applicationName,
                                                        Function<String, String> parserByNameFun,
                                                        ParsingSettingsDto parsingSettings,
                                                        ParsingApplicationSettingsDto appSettings) throws Exception {
        if (parsingSettings.getSingleParser() == null) {
            throw new IllegalArgumentException(MISSING_SINGLE_PARSER);
        }

        return SingleApplicationParser.builder()
                .parser(parsingSettings.getSingleParser().getOutputTopic(),
                        new SerializableSiembolParser(parserByNameFun
                                .apply(parsingSettings.getSingleParser().getParserName())))
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .errorTopic(appSettings.getErrorTopic())
                .originalStringTopic(appSettings.getOriginalStringTopic())
                .maxNumFields(appSettings.getMaxNumFields())
                .maxFieldSize(appSettings.getMaxFieldSize())
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName)
                .build();
    }

    private ParsingApplicationParser createRouterParser(String applicationName,
                                                        Function<String, String> parserByNameFun,
                                                        ParsingSettingsDto parsingSettings,
                                                        ParsingApplicationSettingsDto appSettings) throws Exception {
        if (parsingSettings.getRoutingParser() == null) {
            throw new IllegalArgumentException(MISSING_ROUTING_PARSER);
        }

        RoutingParserDto routingParser = parsingSettings.getRoutingParser();

        RoutingParsingApplicationParser.Builder<RoutingParsingApplicationParser> builder =
                RoutingParsingApplicationParser.builder()
                        .routerParser(new SerializableSiembolParser(
                                parserByNameFun.apply(routingParser.getRouterParserName())))
                        .defaultParser(routingParser.getDefaultParser().getOutputTopic(),
                                new SerializableSiembolParser(
                                        parserByNameFun.apply(routingParser.getDefaultParser().getParserName())))
                        .routingConditionField(routingParser.getRoutingField())
                        .routingMessageField(routingParser.getRoutingMessage())
                        .mergedFields(routingParser.getMergedFields());

        for (RoutedParserPropertiesDto routedParser : routingParser.getParsers()) {
            builder.addParser(routedParser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(
                            parserByNameFun.apply(routedParser.getParserProperties().getParserName())),
                    routedParser.getRoutingFieldPattern());
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .originalStringTopic(appSettings.getOriginalStringTopic())
                .maxNumFields(appSettings.getMaxNumFields())
                .maxFieldSize(appSettings.getMaxFieldSize())
                .metadataPrefix(appSettings.getMetadataPrefix())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .name(applicationName);

        return builder.build();
    }

    private ParsingApplicationParser createHeaderRouterParser(String applicationName,
                                                              Function<String, String> parserByNameFun,
                                                              ParsingSettingsDto parsingSettings,
                                                              ParsingApplicationSettingsDto appSettings) throws Exception {
        var headerRoutingParser = parsingSettings.getHeaderRoutingParserDto();
        if (headerRoutingParser == null) {
            throw new IllegalArgumentException(MISSING_HEADER_ROUTING_PARSER);
        }

        var builder = SourceRoutingApplicationParser.builder()
                .defaultParser(headerRoutingParser.getDefaultParser().getOutputTopic(),
                        new SerializableSiembolParser(
                                parserByNameFun.apply(headerRoutingParser.getDefaultParser().getParserName())));

        for (var parser : headerRoutingParser.getParsers()) {
            builder.addParser(parser.getSourceHeaderValue(),
                    parser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(parserByNameFun.apply(parser.getParserProperties().getParserName())));
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .originalStringTopic(appSettings.getOriginalStringTopic())
                .maxNumFields(appSettings.getMaxNumFields())
                .maxFieldSize(appSettings.getMaxFieldSize())
                .metadataPrefix(appSettings.getMetadataPrefix())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .name(applicationName);

        return builder.build();
    }

    private ParsingApplicationParser createTopicRouterParser(String applicationName,
                                                             Function<String, String> parserByNameFun,
                                                             ParsingSettingsDto parsingSettings,
                                                             ParsingApplicationSettingsDto appSettings) throws Exception {
        var topicRoutingParser = parsingSettings.getTopicRoutingParserDto();
        if (topicRoutingParser == null) {
            throw new IllegalArgumentException(MISSING_TOPIC_ROUTING_PARSER);
        }

        var builder = SourceRoutingApplicationParser.builder()
                .defaultParser(topicRoutingParser.getDefaultParser().getOutputTopic(),
                        new SerializableSiembolParser(
                                parserByNameFun.apply(topicRoutingParser.getDefaultParser().getParserName())));

        for (var parser : topicRoutingParser.getParsers()) {
            builder.addParser(parser.getTopicName(),
                    parser.getParserProperties().getOutputTopic(),
                    new SerializableSiembolParser(parserByNameFun.apply(parser.getParserProperties().getParserName())));
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .originalStringTopic(appSettings.getOriginalStringTopic())
                .maxNumFields(appSettings.getMaxNumFields())
                .maxFieldSize(appSettings.getMaxFieldSize())
                .parseMetadata(appSettings.getParseMetadata())
                .addGuidToMessages(true)
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(applicationName);

        return builder.build();
    }


    private ParsingApplicationParser createParser(ParsingApplicationDto application,
                                                  Function<String, String> parserByNameFun) throws Exception {

        var appSettings = application.getParsingApplicationSettingsDto();
        var parsingSettings = application.getParsingSettingsDto();
        switch (appSettings.getApplicationType()) {
            case SINGLE_PARSER:
                return createSingleParser(application.getParsingApplicationName(),
                        parserByNameFun, parsingSettings, appSettings);
            case ROUTER_PARSING:
                return createRouterParser(application.getParsingApplicationName(),
                        parserByNameFun, parsingSettings, appSettings);
            case TOPIC_ROUTING_PARSING:
                return createTopicRouterParser(application.getParsingApplicationName(),
                        parserByNameFun, parsingSettings, appSettings);
            case HEADER_ROUTING_PARSING:
                return createHeaderRouterParser(application.getParsingApplicationName(),
                        parserByNameFun, parsingSettings, appSettings);
            default:
                throw new IllegalArgumentException(String.format(UNSUPPORTED_PARSER_APP_TYPE,
                        appSettings.getApplicationType()));
        }
    }
    private ParsingApplicationParser createParser(ParsingApplicationDto application,
                                                  String parserConfigs) throws Exception {
        ParsersConfigDto parsers = JSON_PARSERS_CONFIG_READER.readValue(parserConfigs);
        final Map<String, String> parserMap = parsers.getParserConfigurations().stream()
                .collect(Collectors.toMap(ParserConfigDto::getParserName, x -> {
                    try {
                        return JSON_PARSER_CONFIG_WRITER.writeValueAsString(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }));

        Function<String, String> parserByNameFun = x -> getParserFromMap(x, parserMap);
        return createParser(application, parserByNameFun);
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
        attributes.setNumWorkers(application.getParsingApplicationSettingsDto().getNumWorkers());
    }

    private String getParserFromMap(String parserName, Map<String, String> parserMap) {
        if (!parserMap.containsKey(parserName)) {
            String errorMsg = String.format(MISSING_PARSER_MSG, parserName);
            throw new IllegalArgumentException(errorMsg);
        }
        return parserMap.get(parserName);
    }

    private String createDummyParser() throws JsonProcessingException {
        var parser = new ParserConfigDto();
        parser.setParserVersion(1);
        parser.setAuthor("siembol");
        parser.setParserName("dummy_parser");

        var attributes = new ParserAttributesDto();
        attributes.setParserType(ParserTypeDto.GENERIC);
        parser.setParserAttributes(attributes);
        return new ObjectMapper().writerFor(ParserConfigDto.class).writeValueAsString(parser);
    }
}
