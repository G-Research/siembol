package uk.co.gresearch.siembol.configeditor.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class ConfigEditorUtils {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String EMPTY_UI_LAYOUT = "{}";
    private static final ObjectReader UI_CONFIG_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, JsonNode>>() {});
    private static final String SCHEMA_FORM_LAYOUT_KEY = "x-schema-form";
    private static final String INDEX_REPLACE_REGEX = "\"minItems\"\\s*:\\s*1";
    private static final String INDEX_REPLACEMENT = "\"minItems\":0";

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

    public static Optional<String> readTextFromResources(String filename) {
        ClassLoader classLoader = new Object(){}.getClass().getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(filename)) {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = in.read()) != -1) {
                sb.append((char) ch);
            }
            return Optional.of(sb.toString());
        } catch (Exception e) {
            LOG.error("could not get file {}", filename, e);
            return Optional.empty();
        }
    }

    public static Optional<String> readUiLayoutFile(String filePath) {
        try {
            return readTextFromFile(filePath);
        } catch (FileNotFoundException ex) {
            return Optional.of(EMPTY_UI_LAYOUT);
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> readTextFromFile(String filePath) throws IOException{
        try (FileInputStream fs = new FileInputStream(filePath)) {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = fs.read()) != -1) {
                sb.append((char) ch);
            }
            return Optional.of(sb.toString());
        } catch (FileNotFoundException ex) {
            LOG.error("Could not find the file at {}", filePath);
            throw ex;
        } catch (IOException ex) {
            LOG.error("An error occurred while trying to read file {}", filePath);
            throw ex;
        }
    }

    public static Optional<String> patchJsonSchema(String rulesSchema, String uiConfig) throws IOException {
        final DocumentContext context = JsonPath.parse(rulesSchema);
        Map<String, JsonNode> formAttributes = UI_CONFIG_READER.readValue(uiConfig);

        Set<String> layoutKeys = formAttributes.keySet();
        for (String key : layoutKeys) {
            try {
                JsonNode current = context.read(key);
                if (current.isArray()) {
                    //NOTE: json array path return an array of object that should contain an unique object
                    if (current.size() != 1) {
                        LOG.error("Can not find a unique json array path: {} in schema: {}", key, rulesSchema);
                        return Optional.empty();
                    }
                    current = current.get(0);
                }
                if (!current.isObject()) {
                    LOG.error("Path: {} in schema is not an Object, the value: {}", key, current.toString());
                    return Optional.empty();
                }
                ((ObjectNode)current).set(SCHEMA_FORM_LAYOUT_KEY, formAttributes.get(key));
            } catch (Exception e) {
                LOG.error("Problem to find a key: {} in schema: {}", key, rulesSchema);
                return Optional.empty();
            }
        }

        String uiSchema = context.jsonString();
        uiSchema = uiSchema.replaceAll(INDEX_REPLACE_REGEX, INDEX_REPLACEMENT);
        return Optional.ofNullable(uiSchema);
    }
}
