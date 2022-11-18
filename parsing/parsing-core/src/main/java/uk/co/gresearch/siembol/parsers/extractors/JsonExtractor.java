package uk.co.gresearch.siembol.parsers.extractors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * An object for extracting fields from the message using json parsing
 *
 * <p>This derived class of ParserExtractor class is using json parsing to extract fields from the massage.
 * The extractor is parsing a json string recursively in order to create a flat map of extracting fields.
 *
 * @author  Marian Novotny
 * @see ParserExtractor
 *
 */
public class JsonExtractor extends ParserExtractor {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final ObjectReader JSON_READER = new ObjectMapper()
                    .readerFor(new TypeReference<Map<String, Object>>() { });

    private final String nestedSeparator;
    private final String pathPrefix;

    private JsonExtractor(Builder<?> builder) {
        super(builder);
        nestedSeparator = builder.nestedSeparator;
        pathPrefix = builder.pathPrefix;
    }

    @SuppressWarnings("unchecked")
    private void traverseObject(Object current, StringBuilder path, Map<String, Object> result) {
        if (current instanceof Map) {
            Map<String, Object> map = (Map<String, Object>)current;

            final int pathLength = path.length();
            for (String key : map.keySet()) {
                if(pathLength != 0) {
                    path.append(nestedSeparator);
                }
                path.append(key);

                traverseObject(map.get(key), path, result);
                path.setLength(pathLength);
            }
        } else if (current instanceof List) {
            List<Object> list = (List<Object>)current;

            int index = 0;
            final int pathLength = path.length();
            for (Object item : list) {
                if(pathLength != 0) {
                    path.append(nestedSeparator);
                }
                path.append(index++);

                traverseObject(item, path, result);
                path.setLength(pathLength);
            }
        } else if (current instanceof Number
                    || current instanceof Boolean
                    || current instanceof String) {
            result.put(path.toString(), current);
        }
    }

    /**
     * Extracts a message string using a json parser
     *
     * @param message input message to be extracted
     * @return map of string to object with extracted fields
     */
    @Override
    protected Map<String, Object> extractInternally(String message) {
        try {
            Map<String, Object> originalMap = JSON_READER.readValue(message);
            Map<String, Object> result = new HashMap<>();
            StringBuilder path = new StringBuilder(pathPrefix);

            traverseObject(originalMap, path, result);

            return result;
        } catch (Exception e) {
            String errorMessage = String.format("Error during extracting json:%s\n Exception: %s",
                    message, ExceptionUtils.getStackTrace(e));
            LOG.debug(errorMessage);
            if (shouldThrowExceptionOnError()) {
                throw new IllegalStateException(errorMessage);
            }
            return new HashMap<>();
        }
    }

    /**
     * Creates a json extractor builder instance
     *
     * @return json extractor builder instance
     */
    public static Builder<JsonExtractor> builder() {

        return new Builder<>() {
            @Override
            public JsonExtractor build() {
                return new JsonExtractor(this);
            }
        };
    }

    /**
     * A builder for json parser extractor
     *
     * <p>This class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends JsonExtractor>
            extends ParserExtractor.Builder<T> {
        private String nestedSeparator = "_";
        private String pathPrefix = "";

        /**
         * Sets a separator string that will be used to create field names for nested fields.
         * For example { "a" : { "b" : "c" } } will be extracted as ["a_b" -> "c"] using the separator: '_'.
         *
         * @return this builder
         */
        public Builder<T> nestedSeparator(String separator) {
            this.nestedSeparator = separator;
            return this;
        }

        /**
         * Sets a prefix of extracted field names.
         * For example { "a" : "b" } will be extracted as ["test_a" -> "b"] using the prefix: 'test'.
         *
         * @return this builder
         */
        public Builder<T> pathPrefix(String startingPath) {
            this.pathPrefix = startingPath;
            return this;
        }
    }
}
