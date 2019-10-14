package uk.co.gresearch.nortem.configeditor.service.elk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.model.TemplateField;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;


public class ElkProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY_MSG = "Empty template fields";
    private static final String MAPPINGS_FIELD = "mappings";
    private static final String PROPERTIES_FIELD = "properties";
    private static final String TEMPLATE_SUFFIX = "_doc";

    private final HttpProvider httpClient;
    private final String templatePath;

    public ElkProvider(HttpProvider httpClient, String templatePath) {
        this.httpClient = httpClient;
        this.templatePath = templatePath;
    }

    private Optional<String> getSensorName(Iterator<String> it) {
        for (; it.hasNext(); ) {
            String current = it.next();
            if (current.endsWith(TEMPLATE_SUFFIX)
                    && current.length() > TEMPLATE_SUFFIX.length()) {
                return Optional.of(
                        current.substring(0, current.length() - TEMPLATE_SUFFIX.length()));
            }
        }

        return Optional.empty();
    }

    private List<TemplateField> getFields(JsonNode properties) {
        List<TemplateField> ret = new ArrayList<>();
        properties.fieldNames().forEachRemaining(x -> ret.add(new TemplateField(x)));
        return ret;
    }

    public Map<String, List<TemplateField>> getTemplateFields() throws IOException {
        Map<String, List<TemplateField>> ret = new HashMap<>();
        String response = httpClient.get(templatePath);
        LOG.info(String.format("Template to init:\n%s", response));
        JsonNode template = MAPPER.readTree(response);
        for (Iterator<JsonNode> it = template.iterator(); it.hasNext(); ) {
            JsonNode mappings = it.next().get(MAPPINGS_FIELD);
            if (mappings == null) {
                continue;
            }

            Optional<String> sensorName = getSensorName(mappings.fieldNames());
            if (!sensorName.isPresent()) {
                continue;
            }

            List<TemplateField> fields = getFields(mappings.findValue(PROPERTIES_FIELD));
            if (fields.isEmpty()) {
                continue;
            }
            ret.put(sensorName.get(), fields);
        }
        if (ret.isEmpty()) {
            throw new IllegalStateException(EMPTY_MSG);
        }

        LOG.info(String.format("Computed fields:\n%s", ret.toString()));
        return ret;
    }
}
