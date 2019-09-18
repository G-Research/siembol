package uk.co.gresearch.nortem.parsers.extractors;

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
            LOG.error(errorMessage);
            if (shouldThrowExceptionOnError()) {
                throw new IllegalStateException(errorMessage);
            }
            return new HashMap<>();
        }
    }

    public static Builder<JsonExtractor> builder() {

        return new Builder<JsonExtractor>() {
            @Override
            public JsonExtractor build() {
                return new JsonExtractor(this);
            }
        };
    }

    public static abstract class Builder<T extends JsonExtractor>
            extends ParserExtractor.Builder<T> {
        private String nestedSeparator = "_";
        private String pathPrefix = "";

        public Builder<T> nestedSeparator(String separator) {
            this.nestedSeparator = separator;
            return this;
        }

        public Builder<T> pathPrefix(String startingPath) {
            this.pathPrefix = startingPath;
            return this;
        }

    }
}
