package uk.co.gresearch.siembol.configeditor.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.common.ResponseApplicationPaths;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class ResponseHttpProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter ATTRIBUTES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(RespondingResultAttributes.class);
    private static final ObjectReader RESULT_READER = new ObjectMapper()
            .readerFor(RespondingResult.class);
    private final HttpProvider httpProvider;

    private RespondingResult post(ResponseApplicationPaths path, RespondingResultAttributes attributes) throws Exception {
        String body = ATTRIBUTES_WRITER.writeValueAsString(attributes);
        LOG.debug("sending post request to response instance, path: {}", path.getName());
        String json = this.httpProvider.post(path.getName(), body);
        LOG.debug("receiving response from the response instance: {} ", json);
        return RESULT_READER.readValue(json);
    }

    private RespondingResult get(ResponseApplicationPaths path) throws Exception {
        LOG.debug("sending get request to response instance, path: {}", path.getName());
        String json = this.httpProvider.get(path.getName());
        LOG.debug("receiving response from the response instance: {} ", json);
        return RESULT_READER.readValue(json);
    }

    public ResponseHttpProvider(HttpProvider httpProvider) {
        this.httpProvider = httpProvider;
    }

    public RespondingResult getRulesSchema(ConfigEditorUiLayout uiLayout) throws Exception {
        RespondingResult result = get(ResponseApplicationPaths.GET_SCHEMA);
        if (result.getStatusCode() != RespondingResult.StatusCode.OK
                || uiLayout.getConfigLayout() == null
                || uiLayout.getConfigLayout().isEmpty()) {
            return result;
        }


        Optional<String> enrichedSchema = ConfigEditorUtils
                .patchJsonSchema(result.getAttributes().getRulesSchema(), uiLayout.getConfigLayout());
        result.getAttributes().setRulesSchema(enrichedSchema.get());
        return result;

    }

    public RespondingResult getTestSchema(ConfigEditorUiLayout uiLayout) throws Exception {
        RespondingResult result = get(ResponseApplicationPaths.GET_TEST_SCHEMA);
        if (result.getStatusCode() != RespondingResult.StatusCode.OK
                || uiLayout.getConfigLayout() == null
                || uiLayout.getConfigLayout().isEmpty()) {
            return result;
        }

        Optional<String> enrichedSchema = ConfigEditorUtils
                .patchJsonSchema(result.getAttributes().getTestSpecificationSchema(), uiLayout.getTestLayout());
        result.getAttributes().setTestSpecificationSchema(enrichedSchema.get());
        return result;
    }

    public RespondingResult validateRules(String rules) throws Exception {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setJsonRules(rules);
        return post(ResponseApplicationPaths.VALIDATE_RULES, attributes);
    }

    public RespondingResult testRules(String rules, String testSpecification) throws Exception {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setJsonRules(rules);
        attributes.setTestSpecification(testSpecification);
        return post(ResponseApplicationPaths.TEST_RULES, attributes);
    }
}
