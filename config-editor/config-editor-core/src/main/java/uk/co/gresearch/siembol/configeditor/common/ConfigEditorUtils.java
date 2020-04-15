package uk.co.gresearch.siembol.configeditor.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ConfigEditorUtils {
    private static final String EMPTY_UI_LAYOUT = "{\"layout\": {}}";

    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DOT_SEPARATOR_REGEX = "\\.";
    private static final String SCHEMA_FORM_LAYOUT_KEY = "x-schema-form";
    private static final String LAYOUT_FIELD_NAME = "layout";
    private static final String INDEX_REPLACE_REGEX = "\"minItems\"\\s*:\\s*1";
    private static final String INDEX_REPLACEMENT = "\"minItems\":0";

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
            LOG.error("could not get file", e);
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
            LOG.error("Could not find the file at %s", filePath, ex);
            throw ex;
        } catch (IOException ex) {
            LOG.error("An error occurred while trying to read file %s", filePath, ex);
            throw ex;
        }
    }

    public static Optional<String> computeRulesSchema(String rulesSchema, String uiConfig) throws IOException {
        JsonNode schemaNode = MAPPER.readTree(rulesSchema);
        Map<String, Map<String, Object>> formAttributes = MAPPER.readValue(uiConfig,
                new TypeReference<HashMap<String, Map<String, Object>>>() {
                });

        Set<String> layoutKeys = formAttributes.get(LAYOUT_FIELD_NAME).keySet();
        for (String key : layoutKeys) {
            String[] tree = key.split(DOT_SEPARATOR_REGEX);
            JsonNode nodeRef = schemaNode;
            for (String branch : tree) {
                nodeRef = nodeRef.findValue(branch);
            }
            ((ObjectNode) nodeRef).putPOJO(SCHEMA_FORM_LAYOUT_KEY, formAttributes.get(LAYOUT_FIELD_NAME).get(key));
        }

        String uiSchema = MAPPER.writeValueAsString(schemaNode);

        //NOTE: we change min items in arrays to 0 so it displays better in the UI
        uiSchema = uiSchema.replaceAll(INDEX_REPLACE_REGEX, INDEX_REPLACEMENT);
        return Optional.ofNullable(uiSchema);
    }
}
