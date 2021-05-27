package uk.co.gresearch.siembol.configeditor.common;

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
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class ConfigEditorUtils {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader UI_CONFIG_READER = new ObjectMapper()
            .readerFor(ConfigEditorUiLayout.class);
    private static final String INDEX_REPLACE_REGEX = "\"minItems\"\\s*:\\s*1";
    private static final String INDEX_REPLACEMENT = "\"minItems\":0";
    private static final String CONFIG_NAME_FORMAT = "%s.json";
    private static final String TEST_CASE_NAME_FORMAT = "%s-%s.json";
    private static final String TEST_CASE_NAME_PREFIX = "%s-";

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

    public static ConfigEditorUiLayout readUiLayoutFile(String filePath) throws IOException  {
        try (FileInputStream fs = new FileInputStream(filePath)) {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = fs.read()) != -1) {
                sb.append((char) ch);
            }
            return UI_CONFIG_READER.readValue(sb.toString());
        } catch (FileNotFoundException ex) {
            LOG.warn("File {} can not be found, using empty layout instead", filePath);
            return new ConfigEditorUiLayout();
        } catch (IOException ex) {
            LOG.error("Exception {} during reading file {} ", ex, filePath);
            throw ex;
        }
    }

    public static Optional<String> patchJsonSchema(String rulesSchema, Map<String, JsonNode> formAttributes) {
        final DocumentContext context = JsonPath.parse(rulesSchema);
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
                    LOG.error("Path: {} in schema is not an Object, the value: {}", key, current);
                    return Optional.empty();
                }

                if (!formAttributes.get(key).isObject()) {
                    LOG.error("Layout config for path: {} is not an Object: {}",
                            key, formAttributes.get(key).toString());
                    return Optional.empty();
                }

                final ObjectNode currentObjectNode = (ObjectNode)current;
                formAttributes.get(key).fieldNames().forEachRemaining(
                        fieldName -> currentObjectNode.set(fieldName, formAttributes.get(key).get(fieldName)));
            } catch (Exception e) {
                LOG.error("Problem to find a key: {} in schema: {}", key, rulesSchema);
                return Optional.empty();
            }
        }

        String uiSchema = context.jsonString();
        uiSchema = uiSchema.replaceAll(INDEX_REPLACE_REGEX, INDEX_REPLACEMENT);
        return Optional.of(uiSchema);
    }

    public static JsonNode evaluateJsonPath(String jsonString, String jsonPath) {
        final DocumentContext context = JsonPath.parse(jsonString);
        return context.read(jsonPath);
    }

    public static String getConfigNameFileName(String configName) {
        return String.format(CONFIG_NAME_FORMAT, configName);
    }

    public static String getTestCaseFileName(String configName, String testCaseName) {
        return String.format(TEST_CASE_NAME_FORMAT, configName, testCaseName);
    }

    public static String getTestCaseFileNamePrefix(String configName) {
        return String.format(TEST_CASE_NAME_PREFIX, configName);
    }

    public static String getNormalisedConfigName(String configName) {
        return configName.trim().replaceAll(" ", "_");
    }

}
