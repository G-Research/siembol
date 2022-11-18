package uk.co.gresearch.siembol.parsers.factory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.parsers.common.SiembolParser;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import uk.co.gresearch.siembol.parsers.generic.SiembolGenericParser;
import uk.co.gresearch.siembol.parsers.netflow.SiembolNetflowParser;
import uk.co.gresearch.siembol.parsers.syslog.SiembolSyslogParser;
import uk.co.gresearch.siembol.parsers.extractors.*;
import uk.co.gresearch.siembol.parsers.model.*;
import uk.co.gresearch.siembol.parsers.transformations.Transformation;
import uk.co.gresearch.siembol.parsers.transformations.TransformationFactory;
import uk.co.gresearch.siembol.common.result.SiembolResult;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static uk.co.gresearch.siembol.parsers.model.PreProcessingFunctionDto.STRING_REPLACE;
/**
 * An object for compiling parsers
 *
 * <p>This class is an implementation of ParserFactory interface.
 * It is used for creating a parser, testing a parser on input, validating a parser configuration and
 * providing the json schema for parser configurations.
 *
 * @author  Marian Novotny
 * @see ParserFactory
 * @see ParserFactoryResult
 *
 */
public class ParserFactoryImpl implements ParserFactory {
    private static final ObjectReader JSON_PARSER_CONFIG_READER =
            new ObjectMapper().readerFor(ParserConfigDto.class);
    private static final ObjectReader JSON_PARSERS_CONFIG_READER =
            new ObjectMapper().readerFor(ParsersConfigDto.class);
    private static final ObjectWriter JSON_PARSERS_CONFIG_WRITER =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerFor(ParsersConfigDto.class);
    private static final ObjectWriter JSON_PARSER_CONFIG_WRITER =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerFor(ParserConfigDto.class);

    private final JsonSchemaValidator jsonSchemaValidator;
    private final TransformationFactory transformationFactory = new TransformationFactory();

    ParserFactoryImpl(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserFactoryResult getSchema() {
        ParserFactoryAttributes attributes = new ParserFactoryAttributes();
        attributes.setJsonSchema(jsonSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserFactoryResult create(String parserConfigStr) {
        try {
            ParserConfigDto parserConfig = JSON_PARSER_CONFIG_READER.readValue(parserConfigStr);
            List<ParserExtractor> extractors = createParserExtractors(parserConfig)
                    .orElse(null);
            List<Transformation> transformations = createTransformations(parserConfig)
                    .orElse(null);

            SiembolParser parser;
            switch (parserConfig.getParserAttributes().getParserType()) {
                case NETFLOW:
                    parser = new SiembolNetflowParser();
                    break;
                case SYSLOG:
                    parser = createSyslogParser(parserConfig.getParserAttributes().getSyslogConfig(), extractors, transformations);
                    break;
                case GENERIC:
                    parser = new SiembolGenericParser(extractors, transformations);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported parser type");
            }

            ParserFactoryAttributes attributes = new ParserFactoryAttributes();
            attributes.setSiembolParser(parser);
            attributes.setExtractors(extractors);
            attributes.setTransformations(transformations);
            attributes.setParserName(parserConfig.getParserName());
            return new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, attributes);

        } catch (Exception e) {
            String message = String.format("Error during parser compilation: %s", ExceptionUtils.getStackTrace(e));
            return ParserFactoryResult.fromErrorMessage(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserFactoryResult test(String parserConfig, String metadata, byte[] rawLog) {
        SerializableSiembolParser parser;
        try {
            parser = new SerializableSiembolParser(parserConfig);
        } catch (Exception e) {
            return ParserFactoryResult.fromErrorMessage(ExceptionUtils.getStackTrace(e));
        }
        
        ParserResult parserResult = parser.parseToResult(metadata, rawLog);
        ParserFactoryAttributes attributes = new ParserFactoryAttributes();
        attributes.setParserResult(parserResult);
        return new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserFactoryResult validateConfiguration(String parserConfig) {
        try {
            String configurations = wrapParserConfigToParsersConfig(parserConfig);
            return validateConfigurations(configurations);
        } catch (IOException e) {
            return ParserFactoryResult.fromErrorMessage(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserFactoryResult validateConfigurations(String parserConfigurations) {
        try {
            SiembolResult validationResult  = jsonSchemaValidator.validate(parserConfigurations);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return ParserFactoryResult.fromErrorMessage(validationResult.getAttributes().getMessage());
            }

            ParsersConfigDto parsersConfig = JSON_PARSERS_CONFIG_READER.readValue(parserConfigurations);
            for (ParserConfigDto config : parsersConfig.getParserConfigurations()) {
                String configStr = JSON_PARSER_CONFIG_WRITER.writeValueAsString(config);
                ParserFactoryResult createResult = create(configStr);
                if (createResult.getStatusCode() != ParserFactoryResult.StatusCode.OK) {
                    return createResult;
                }

            }
            return new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, new ParserFactoryAttributes());

        } catch(Exception e){
            return ParserFactoryResult.fromErrorMessage(ExceptionUtils.getStackTrace(e));
        }
    }

    public static ParserFactory createParserFactory() throws Exception {
        JsonSchemaValidator validator = new SiembolJsonSchemaValidator(ParsersConfigDto.class);
        return new ParserFactoryImpl(validator);
    }

    private EnumSet<ParserExtractor.ParserExtractorFlags> getExtractorFlags(ExtractorAttributesDto attributes) {
        EnumSet<ParserExtractor.ParserExtractorFlags> ret = EnumSet.noneOf(
                ParserExtractor.ParserExtractorFlags.class);

        if (attributes.getShouldOverwrite()) {
            ret.add(ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        }

        if (attributes.getShouldRemoveField()) {
            ret.add(ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        }

        if (attributes.getRemoveQuotes()) {
            ret.add(KeyValueExtractor.ParserExtractorFlags.REMOVE_QUOTES);
        }

        if (attributes.getThrownExceptionOnError()) {
            ret.add(KeyValueExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);
        }

        if (attributes.getSkipEmptyValues()) {
            ret.add(KeyValueExtractor.ParserExtractorFlags.SKIP_EMPTY_VALUES);
        }

        return ret;
    }

    private EnumSet<KeyValueExtractor.KeyValueExtractorFlags> getKeyValueExtractorFlags(
            ExtractorAttributesDto attributes) {
        EnumSet<KeyValueExtractor.KeyValueExtractorFlags> ret = EnumSet.noneOf(
                KeyValueExtractor.KeyValueExtractorFlags.class);

        if (attributes.getRenameDuplicateKeys()) {
            ret.add(KeyValueExtractor.KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS);
        }

        if (attributes.getQuotaHandling()) {
            ret.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);
        }

        if (attributes.getEscapingHandling()) {
            ret.add(KeyValueExtractor.KeyValueExtractorFlags.ESCAPING_HANDLING);
        }

        if (attributes.getNextKeyStrategy()) {
            ret.add(KeyValueExtractor.KeyValueExtractorFlags.NEXT_KEY_STRATEGY);
        }

        return ret;
    }

    private EnumSet<PatternExtractor.PatternExtractorFlags> getPatternExtractorFlags(
            ExtractorAttributesDto attributes) {
        EnumSet<PatternExtractor.PatternExtractorFlags> ret = EnumSet.noneOf(
                PatternExtractor.PatternExtractorFlags.class);

        if (attributes.getShouldMatchPattern()) {
            ret.add(PatternExtractor.PatternExtractorFlags.SHOULD_MATCH);
        }

        if (attributes.getDotAllRegexFlag()) {
            ret.add(PatternExtractor.PatternExtractorFlags.DOTALL);
        }
        return ret;
    }

    private EnumSet<JsonPathExtractor.JsonPathExtractorFlags> getJsonPathExtractorFlags(
            ExtractorAttributesDto attributes) {
        var ret = EnumSet.noneOf(
                JsonPathExtractor.JsonPathExtractorFlags.class);

        if (attributes.getAtLeastOneQueryResult()) {
            ret.add(JsonPathExtractor.JsonPathExtractorFlags.AT_LEAST_ONE_QUERY_RESULT);
        }

        return ret;
    }

    private List<ColumnNames> createColumnNames(List<ColumnNamesDto> columnNames) {
        return columnNames.stream()
                .map(x -> x.getColumnFilter() == null
                        ? new ColumnNames(x.getNames())
                        : new ColumnNames(x.getNames(), new AbstractMap.SimpleEntry<>(
                        x.getColumnFilter().getIndex(), x.getColumnFilter().getRequiredValue())))
                .collect(Collectors.toList());
    }

    private List<Pair<String, String>> createPatterns(List<SearchPatternDto> patterns) {
        return patterns
                .stream()
                .map(x -> Pair.of(x.getName(), x.getPattern()))
                .collect(Collectors.toList());
    }

    private Function<String, String> createPreprocessingFunction(ParserExtractorDto extractor) {
        if (extractor.getPreProcessingFunction() == null) {
            return null;
        }

        switch(extractor.getPreProcessingFunction()) {
            case STRING_REPLACE:
            case STRING_REPLACE_ALL:
                if (extractor.getAttributes().getStringReplaceTarget() == null
                        || extractor.getAttributes().getStringReplaceReplacement() == null) {
                    throw new IllegalArgumentException(
                            "string_replace requires attributes: string_replace_target, string_replace_replacement");
                }
                final String target = extractor.getAttributes().getStringReplaceTarget();
                final String replacement = extractor.getAttributes().getStringReplaceReplacement();
                if (extractor.getPreProcessingFunction() == STRING_REPLACE) {
                    return x -> ParserExtractorLibrary.replace(x, target, replacement);
                } else {
                    return x -> ParserExtractorLibrary.replaceAll(x, target, replacement);
                }
        }
        return null;
    }

    private List<Function<Map<String, Object>, Map<String, Object>>> createPostProcessingFunctions(
            ParserExtractorDto extractor) {

        List<Function<Map<String, Object>, Map<String, Object>>> ret = new ArrayList<>();
        if (extractor.getPostProcessingFunctions() == null) {
            return ret;
        }

        for (PostProcessingFunctionDto fun : extractor.getPostProcessingFunctions()) {
            final String timeField = extractor.getAttributes().getTimestampField();
            switch (fun) {
                case CONVERT_UNIX_TIMESTAMP:
                    ret.add(x -> ParserExtractorLibrary.convertUnixTimestampToMs(x, timeField));
                    break;
                case FORMAT_TIMESTAMP:
                    if (extractor.getAttributes().getTimeFormats() == null) {
                        throw new IllegalArgumentException(
                                "format_timestamp requires times_formats in attributes");
                    }

                    final ArrayList<ParserDateFormat> dateFormats = new ArrayList<>();
                    for (TimeFormatDto timeFormat : extractor.getAttributes().getTimeFormats()) {
                        dateFormats.add(new ParserDateFormat(timeFormat.getTimeFormat(),
                                java.util.Optional.ofNullable(timeFormat.getTimezone()),
                                java.util.Optional.ofNullable(timeFormat.getValidationRegex())));
                    }
                    ret.add(x -> ParserExtractorLibrary.formatTimestampToMs(dateFormats, x, timeField));
                    break;
                case CONVERT_TO_STRING:
                    if (extractor.getAttributes().getConversionExclusions() == null) {
                        throw new IllegalArgumentException(
                                "convert_to_string requires conversion_exclusions in attributes");
                    }
                    final Set<String> exclusions = new HashSet<>(extractor.getAttributes().getConversionExclusions());
                    ret.add(x -> ParserExtractorLibrary.convertToString(x, exclusions));
                    break;
            }
        }
        return ret;
    }

    private ParserExtractor createPatternExtractor(ParserExtractorDto extractor) {
        return PatternExtractor
                .builder()
                .patterns(extractor.getAttributes().getRegularExpressions())
                .patternExtractorFlags(getPatternExtractorFlags(extractor.getAttributes()))
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createCSVExtractor(ParserExtractorDto extractor) {
        return CSVExtractor
                .builder()
                .columnNames(createColumnNames(extractor.getAttributes().getColumnNames()))
                .wordDelimiter(extractor.getAttributes().getWordDelimiter())
                .skippingColumnName(extractor.getAttributes().getSkippingColumnName())
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createKeyValueExtractor(ParserExtractorDto extractor) {

        if (extractor.getAttributes().getWordDelimiter().length() > 1
                || extractor.getAttributes().getKeyValueDelimiter().length() > 1) {
            throw new IllegalArgumentException("Key value Extractor supports only one character delimiters");
        }

        return KeyValueExtractor
                .builder()
                .keyValueExtractorFlags(getKeyValueExtractorFlags(extractor.getAttributes()))
                .wordDelimiter(extractor.getAttributes().getWordDelimiter().charAt(0))
                .keyValueDelimiter(extractor.getAttributes().getKeyValueDelimiter().charAt(0))
                .escapedChar(extractor.getAttributes().getEscapedCharacter())
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createJsonExtractor(ParserExtractorDto extractor) {
        return JsonExtractor
                .builder()
                .pathPrefix(extractor.getAttributes().getPathPrefix())
                .nestedSeparator(extractor.getAttributes().getNestedSeparator())
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createRegexSelectExtractor(ParserExtractorDto extractor) {
        return RegexSelectExtractor
                .builder()
                .patterns(createPatterns(extractor.getAttributes().getRegexSelectConfig().getPatterns()))
                .outputField(extractor.getAttributes().getRegexSelectConfig().getOutputField())
                .defaultValue(extractor.getAttributes().getRegexSelectConfig().getDefaultValue())
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createJsonPathExtractor(ParserExtractorDto extractor) {
        var builder = JsonPathExtractor
                .builder()
                .jsonPathExtractorFlags(getJsonPathExtractorFlags(extractor.getAttributes()));

        extractor.getAttributes().getJsonPathQueries()
                .forEach(x -> builder.addQuery(x.getOutputField(), x.getQuery()));

        return builder
                .name(extractor.getName())
                .field(extractor.getField())
                .extractorFlags(getExtractorFlags(extractor.getAttributes()))
                .preProcessing(createPreprocessingFunction(extractor))
                .postProcessing(createPostProcessingFunctions(extractor))
                .build();
    }

    private ParserExtractor createParserExtractor(ParserExtractorDto extractor) {
        if (extractor == null
                || extractor.getName() == null
                || extractor.getField() == null
                || extractor.getType() == null
                || extractor.getAttributes() == null) {
            throw new IllegalArgumentException("Missing fields required to create ParserExtractor");
        }

        switch (extractor.getType()) {
            case PATTERN_EXTRACTOR:
                return createPatternExtractor(extractor);
            case KEY_VALUE_EXTRACTOR:
                return createKeyValueExtractor(extractor);
            case CSV_EXTRACTOR:
                return createCSVExtractor(extractor);
            case JSON_EXTRACTOR:
                return createJsonExtractor(extractor);
            case REGEX_SELECT_EXTRACTOR:
                return createRegexSelectExtractor(extractor);
            case JSON_PATH_EXTRACTOR:
                return createJsonPathExtractor(extractor);
        }

        throw new IllegalArgumentException("Unsupported extractor type");
    }

    private Optional<ArrayList<ParserExtractor>> createParserExtractors(ParserConfigDto parserConfig) {
        if (parserConfig.getParserExtractors() == null) {
            return Optional.empty();
        }

        ArrayList<ParserExtractor> ret = parserConfig.getParserExtractors().stream()
                .filter(x -> x.isEnabled())
                .map(x -> createParserExtractor(x))
                .collect(toCollection(ArrayList::new));

        return ret.isEmpty() ? Optional.empty() : Optional.of(ret);
    }

    private String wrapParserConfigToParsersConfig(String configStr) throws IOException {
        ParserConfigDto config = JSON_PARSER_CONFIG_READER.readValue(configStr);
        ParsersConfigDto parsersConfig = new ParsersConfigDto();
        parsersConfig.setParserVersion(config.getParserVersion());

        parsersConfig.setParserConfigurations(Arrays.asList(config));
        return JSON_PARSERS_CONFIG_WRITER.writeValueAsString(parsersConfig);
    }

    private Optional<ArrayList<Transformation>> createTransformations(ParserConfigDto parserConfig) {
        if (parserConfig.getParserTransformations() == null) {
            return Optional.empty();
        }

        ArrayList<Transformation> ret = parserConfig.getParserTransformations().stream()
                .filter(x -> x.isEnabled())
                .map(x -> transformationFactory.create(x))
                .collect(toCollection(ArrayList::new));

        return ret.isEmpty() ? Optional.empty() : Optional.of(ret);
    }

    private SiembolParser createSyslogParser(SyslogParserConfigDto syslogConfig,
                                             List<ParserExtractor> extractors,
                                             List<Transformation> transformations) {
        SiembolSyslogParser.Builder builder = new SiembolSyslogParser.Builder()
                .flags(getSyslogFlags(syslogConfig))
                .timezone(syslogConfig.getTimezone())
                .extractors(extractors)
                .transformations(transformations);

        if (syslogConfig.getTimeFormats() != null) {
            builder.dateFormats(
                    syslogConfig.getTimeFormats().stream()
                            .map(x -> new ParserDateFormat(x.getTimeFormat(),
                                    Optional.ofNullable(x.getTimezone()),
                                    Optional.ofNullable(x.getValidationRegex())))
                            .collect(Collectors.toList()));
        }

        return builder.build();

    }

    private EnumSet<SiembolSyslogParser.Flags> getSyslogFlags(SyslogParserConfigDto syslogConfig) {
        EnumSet<SiembolSyslogParser.Flags> ret = EnumSet.noneOf(SiembolSyslogParser.Flags.class);
        if (syslogConfig.getMergeSdElements()) {
            ret.add(SiembolSyslogParser.Flags.MERGE_SD_ELEMENTS);
        }

        if (syslogConfig.getSyslogVersion() == SyslogVersionDto.RFC_3164
                || syslogConfig.getSyslogVersion() == SyslogVersionDto.RFC_3164_RFC_5424) {
            ret.add(SiembolSyslogParser.Flags.EXPECT_RFC_3164_VERSION);
        }

        if (syslogConfig.getSyslogVersion() == SyslogVersionDto.RFC_5424
                || syslogConfig.getSyslogVersion() == SyslogVersionDto.RFC_3164_RFC_5424) {
            ret.add(SiembolSyslogParser.Flags.EXPECT_RFC_RFC5424_VERSION);
        }

        return ret;
    }
}
