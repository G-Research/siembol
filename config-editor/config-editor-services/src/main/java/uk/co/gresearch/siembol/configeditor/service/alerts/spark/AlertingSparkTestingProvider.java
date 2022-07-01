package uk.co.gresearch.siembol.configeditor.service.alerts.spark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlertingSparkTestingProvider {
    private static final String BATCHES_API = "batches";
    private static final String CLASS_NAME_ATTRIBUTE = "className";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String ARGS_NAME_ATTRIBUTE = "args";

    private static final ObjectWriter ATTRIBUTES_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() {
            });
    private final HttpProvider httpProvider;
    private final Map<String, Object> sparkAttributes;

    public AlertingSparkTestingProvider(HttpProvider httpProvider,
                                        Map<String, Object> sparkAttributes) {
        this.sparkAttributes = new HashMap<>();
        this.sparkAttributes.put(CLASS_NAME_ATTRIBUTE, "uk.co.gresearch.siembol.spark.AlertingSpark");
        this.sparkAttributes.put(NAME_ATTRIBUTE, "siembol_alerting_tester");
        this.sparkAttributes.putAll(sparkAttributes);
        this.httpProvider = httpProvider;
    }

    public String submitJob(String argument) throws Exception {
        var currentAttributes = new HashMap<>(sparkAttributes);
        currentAttributes.put(ARGS_NAME_ATTRIBUTE, List.of(argument));
        String body = ATTRIBUTES_WRITER.writeValueAsString(currentAttributes);
        return httpProvider.post(BATCHES_API, body);
    }
}
