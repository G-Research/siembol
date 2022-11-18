package uk.co.gresearch.siembol.parsers.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import java.util.*;
/**
 * An object for extracting fields from the message by evaluating json path queries
 *
 * <p>This derived class of ParserExtractor class is using json path query to extract fields from the massage.
 * The extractor is evaluating json path queries in order to create a map of extracting fields.
 *
 * @author  Marian Novotny
 * @see ParserExtractor
 *
 */
public class JsonPathExtractor extends ParserExtractor {
    public enum JsonPathExtractorFlags {
        AT_LEAST_ONE_QUERY_RESULT
    }

    static {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String AT_LEAST_ONE_QUERY_MSG = "At least one json path query should store its result";
    private static final String EMPTY_FIELD_OR_QUERY_MSG = "Output field and json path query should be non empty";
    private static final String EMPTY_QUERIES_MSG = "Json path extractor requires at least one query";
    private static final String EXCEPTION_MSG = "Error during evaluating json path extractor name:%s," +
            " message:%s, exception: %s";

    private final ArrayList<ImmutablePair<String, String>> queries;
    private final EnumSet<JsonPathExtractorFlags> jsonPathExtractorFlags;

    private JsonPathExtractor(JsonPathExtractor.Builder<?> builder) {
        super(builder);
        queries = builder.queries;
        jsonPathExtractorFlags = builder.jsonPathExtractorFlags;
    }

    private String getArrayValue(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        node.iterator().forEachRemaining(x -> sb.append(x.isTextual() ? x.textValue() : x.toString()).append(','));
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    private Optional<Object> getValue(DocumentContext context, String jsonPathQuery) {
        Object currentObj;
        try {
            currentObj = context.read(jsonPathQuery);
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }

        if (currentObj instanceof Number) {
            return Optional.of(currentObj);
        }

        if (!(currentObj instanceof JsonNode)) {
            return Optional.empty();
        }

        JsonNode current = (JsonNode) currentObj;
        if (current.isArray()) {
            if (current.size() == 1) {
                current = current.get(0);
            } else {
                return Optional.of(getArrayValue(current));
            }
        }

        if (current.isBoolean()) {
            return Optional.of(current.booleanValue());
        }

        if (current.isNumber()) {
            return Optional.of(current.numberValue());
        }

        if (current.isTextual()) {
            return Optional.of(current.textValue());
        }

        return Optional.empty();
    }

    /**
     * Extracts fields from a message string using json path queries
     *
     * @param message input message to be extracted
     * @return map of string to object with extracted fields
     */
    @Override
    protected Map<String, Object> extractInternally(String message) {
        Map<String, Object> result = new HashMap<>();
        try {
            final DocumentContext context = JsonPath.parse(message);
            for (var query : queries) {
                getValue(context, query.getRight())
                        .ifPresent(x -> result.put(query.getLeft(), x));
            }
        } catch (Exception e) {
            String errorMessage = String.format(EXCEPTION_MSG, getName(), message, ExceptionUtils.getStackTrace(e));
            LOG.debug(errorMessage);
            if (shouldThrowExceptionOnError()) {
                throw new IllegalStateException(errorMessage);
            }
        }

        if (result.isEmpty()
                && jsonPathExtractorFlags.contains(JsonPathExtractorFlags.AT_LEAST_ONE_QUERY_RESULT)
                && shouldThrowExceptionOnError()) {
            throw new IllegalStateException(AT_LEAST_ONE_QUERY_MSG);
        }

        return result;
    }

    /**
     * Creates a json path extractor builder instance
     *
     * @return json path extractor builder instance
     */
    public static Builder<JsonPathExtractor> builder() {

        return new Builder<>() {
            @Override
            public JsonPathExtractor build() {
                if (queries.isEmpty()) {
                    throw new IllegalArgumentException(EMPTY_QUERIES_MSG);
                }
                return new JsonPathExtractor(this);
            }
        };
    }

    /**
     * A builder for json path extractor
     *
     * <p>This class is using Builder pattern.
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends JsonPathExtractor>
            extends ParserExtractor.Builder<T> {

        protected ArrayList<ImmutablePair<String, String>> queries = new ArrayList<>();
        protected EnumSet<JsonPathExtractorFlags> jsonPathExtractorFlags = EnumSet.noneOf(JsonPathExtractorFlags.class);

        /**
         * Adds a json path query string.
         * It supports a dot and a bracket notation using syntax from
         * <a href="https://github.com/json-path/JsonPath#readme">https://github.com/json-path/JsonPath#readme</a> .
         *
         * @param field A field name for storing query result
         * @param query A json path query
         * @return this builder
         */
        public Builder<T> addQuery(String field, String query) {
            if (Strings.isNullOrEmpty(field) || Strings.isNullOrEmpty(query)) {
                throw new IllegalArgumentException(EMPTY_FIELD_OR_QUERY_MSG);
            }

            queries.add(ImmutablePair.of(field, query));
            return this;
        }

        /**
         * Sets json path extractor flags
         *
         * @param jsonPathExtractorFlags Json path extractor flags
         * @return this builder
         * @see JsonPathExtractorFlags
         */
        public Builder<T> jsonPathExtractorFlags(EnumSet<JsonPathExtractorFlags> jsonPathExtractorFlags) {
            this.jsonPathExtractorFlags = jsonPathExtractorFlags;
            return this;
        }
    }
}