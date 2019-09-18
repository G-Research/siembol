package uk.co.gresearch.nortem.parsers.application.factory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.parsers.application.model.*;
import uk.co.gresearch.nortem.parsers.application.parsing.ParsingApplicationParser;
import uk.co.gresearch.nortem.parsers.application.parsing.RoutingParsingApplicationParser;
import uk.co.gresearch.nortem.parsers.application.parsing.SingleApplicationParser;
import uk.co.gresearch.nortem.parsers.common.SerializableNortemParser;
import uk.co.gresearch.nortem.parsers.factory.ParserFactory;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult;
import uk.co.gresearch.nortem.parsers.model.ParserConfigDto;
import uk.co.gresearch.nortem.parsers.model.ParsersConfigDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.OK;
import static uk.co.gresearch.nortem.parsers.application.model.ParsingApplicationTypeDto.SINGLE_PARSER;

public class ParsingApplicationFactoryImpl implements ParsingApplicationFactory {
    private static final String MISSING_PARSER_MSG = "Missing parser: %s in parser configurations";
    private static final String MISSING_SINGLE_PARSER = "Missing single_parser properties";
    private static final String MISSING_ROUTING_PARSER = "Missing routing_parser properties";

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
        jsonSchemaValidator =  new NortemJsonSchemaValidator(ParsingApplicationsDto.class);
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
            attributes.setName(application.getParsingApplicationName());
            attributes.setInputParallelism(application.getParsingApplicationSettingsDto().getInputParallelism());
            attributes.setOutputParallelism(application.getParsingApplicationSettingsDto().getOutputParallelism());
            attributes.setParsingParallelism(application.getParsingApplicationSettingsDto().getParsingParallelism());
            attributes.setInputTopics(application.getParsingApplicationSettingsDto().getInputTopics());

            attributes.setApplicationParser(createParser(application, parserConfigs));
        } catch (Exception e) {
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
        return new ParsingApplicationFactoryResult(OK, attributes);
    }

    @Override
    public ParsingApplicationFactoryResult validateConfiguration(String parserConfig) {
        try {
            String applications = wrapParserApplicationToParserApplications(parserConfig);
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
            NortemResult validationResult  = jsonSchemaValidator.validate(parserConfigurations);
            if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
                attributes.setMessage(validationResult.getAttributes().getMessage());
                return new ParsingApplicationFactoryResult(ERROR, attributes);
            }

            return new ParsingApplicationFactoryResult(OK, attributes);
        } catch(Exception e){
            attributes.setMessage(ExceptionUtils.getStackTrace(e));
            return new ParsingApplicationFactoryResult(ERROR, attributes);
        }
    }

    private ParsingApplicationParser createParser(ParsingApplicationDto application,
                                                  String parserConfigs) throws Exception {
        ParsersConfigDto parsers = JSON_PARSERS_CONFIG_READER.readValue(parserConfigs);
        Map<String, String> parsersMap = parsers.getParserConfigurations().stream()
                .collect(Collectors.toMap(x -> x.getParserName(), x -> {
                    try {
                        return JSON_PARSER_CONFIG_WRITER.writeValueAsString(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }));

        ParsingApplicationSettingsDto appSettings = application.getParsingApplicationSettingsDto();
        ParsingSettingsDto parsingSettingsDto = application.getParsingSettingsDto();

        if (appSettings.getApplicationType() == SINGLE_PARSER) {
            if (parsingSettingsDto.getSingleParser() == null) {
                throw new IllegalArgumentException(MISSING_SINGLE_PARSER);
            }

            if (!parsersMap.containsKey(parsingSettingsDto.getSingleParser().getParserName())) {
                String errorMsg = String.format(MISSING_PARSER_MSG,
                        parsingSettingsDto.getSingleParser().getParserName());
                throw new IllegalArgumentException(errorMsg);
            }

            return SingleApplicationParser.builder()
                    .parser(parsingSettingsDto.getSingleParser().getOutputTopic(),
                            new SerializableNortemParser(parsersMap
                                    .get(parsingSettingsDto.getSingleParser().getParserName())))
                    .errorTopic(appSettings.getErrorTopic())
                    .metadataPrefix(appSettings.getMetadataPrefix())
                    .name(application.getParsingApplicationName())
                    .build();
        }

        if (parsingSettingsDto.getRoutingParser() == null) {
            throw new IllegalArgumentException(MISSING_ROUTING_PARSER);
        }

        RoutingParserDto routingParser = parsingSettingsDto.getRoutingParser();
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
                        .routerParser(new SerializableNortemParser(parsersMap.get(routingParser.getRouterParserName())))
                        .defaultParser(routingParser.getDefaultParser().getOutputTopic(),
                                new SerializableNortemParser(
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
                    new SerializableNortemParser(parsersMap.get(routedParser.getParserProperties().getParserName())),
                    routedParser.getRoutingFieldPattern());
        }

        builder.errorTopic(appSettings.getErrorTopic())
                .parseMetadata(appSettings.getParseMetadata())
                .metadataPrefix(appSettings.getMetadataPrefix())
                .name(application.getParsingApplicationName());

        return builder.build();
    }

    private String wrapParserApplicationToParserApplications(String configStr) throws IOException {
        ParsingApplicationDto application = JSON_PARSING_APP_READER.readValue(configStr);
        ParsingApplicationsDto applications = new ParsingApplicationsDto();
        applications.setParsingApplicationsVersion(application.getParsingApplicationVersion());

        applications.setParsingApplications(Arrays.asList(application));
        return JSON_PARSING_APPS_WRITER.writeValueAsString(applications);
    }
}
