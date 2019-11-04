package uk.co.gresearch.nortem.configeditor.service.centrifuge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.RulesWrapperDto;
import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto;
import uk.co.gresearch.nortem.configeditor.model.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class CentrifugeRuleSchemaImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String EMPTY_UI_CONFIG = "Empty UI config provided";

    private final CentrifugeSchemaService centrifugeSchemaService;
    private final AtomicReference<Exception> exception = new AtomicReference<>();

    CentrifugeRuleSchemaImpl(CentrifugeSchemaService centrifugeSchemaService) {
        this.centrifugeSchemaService = centrifugeSchemaService;
    }

    public static ConfigSchemaService createCentrifugeRuleSchemaImpl(String centrifugeUrl,
                                                                     Optional<String> uiConfig,
                                                                     Optional<String> testUiConfig) throws Exception {
        HttpProvider httpProvider = new HttpProvider(centrifugeUrl, HttpProvider::getKerberosHttpClient);
        if (!uiConfig.isPresent()) {
            throw new IllegalArgumentException(EMPTY_UI_CONFIG);
        }

        CentrifugeSchemaService centrifugeSchemaService = new CentrifugeSchemaService
                .Builder(httpProvider, uiConfig.get())
                .uiTestConfig(testUiConfig)
                .build();

        return new CentrifugeRuleSchemaImpl(centrifugeSchemaService);
    }

    @Override
    public ConfigEditorResult getSchema() {
        String schema = this.centrifugeSchemaService.getRulesSchema();
        ConfigEditorAttributes result = new ConfigEditorAttributes();
        result.setRulesSchema(schema);
        return new ConfigEditorResult(OK, result);
    }

    @Override
    public ConfigEditorResult getFields() {
        ConfigEditorAttributes result = new ConfigEditorAttributes();
        result.setFields(centrifugeSchemaService.getFields());
        return new ConfigEditorResult(OK, result);
    }

    private String wrapSingleRuleAsRules(String rule) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, Object.class);
        Map<String, Object> ruleObj = mapper.readValue(rule, mapType);
        RulesWrapperDto wrapper = new RulesWrapperDto();
        wrapper.addToRules(ruleObj);

        return mapper.writeValueAsString(wrapper);
    }

    @Override
    public ConfigEditorResult validateConfiguration(String rule) {
        try{
            String ruleAsRules = wrapSingleRuleAsRules(rule);
            return validateConfigurations(ruleAsRules);
        } catch (IOException e){
            LOG.error(e.getMessage());
            ConfigEditorAttributes result = new ConfigEditorAttributes();
            result.setException(e.toString());
            result.setMessage(ExceptionUtils.getMessage(e));
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, result);
        }
    }

    @Override
    public ConfigEditorResult validateConfigurations(String rules) {
        try {
            ConfigEditorAttributes result = new ConfigEditorAttributes();
            CentrifugeResponseDto response = this.centrifugeSchemaService.validateRules(rules);
            ConfigEditorResult.StatusCode statusCode;
            switch(response.getStatusCode()) {
                case OK:
                    statusCode = OK;
                    exception.set(null);
                    break;
                case BAD_REQUEST:
                    statusCode = ConfigEditorResult.StatusCode.BAD_REQUEST;
                    break;
                case ERROR:
                    statusCode = ConfigEditorResult.StatusCode.ERROR;
                    break;
                case UNAUTHORISED:
                    statusCode = ConfigEditorResult.StatusCode.UNAUTHORISED;
                    break;
                default:
                    statusCode = ConfigEditorResult.StatusCode.ERROR;
            }
            result.setException(response.getAttributes().getException());
            result.setMessage(response.getAttributes().getMessage());

            return new ConfigEditorResult(statusCode, result);
        } catch (IOException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            exception.set(e);
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult testConfiguration(String rule, String event) {
        try{
            String ruleAsRules = wrapSingleRuleAsRules(rule);
            return testConfigurations(ruleAsRules, event);
        } catch (IOException e){
            LOG.error(e.getMessage());
            ConfigEditorAttributes result = new ConfigEditorAttributes();
            result.setException(e.toString());
            result.setMessage(ExceptionUtils.getMessage(e));
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, result);
        }
    }

    @Override
    public ConfigEditorResult testConfigurations(String rules, String event) {
        try {
            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            CentrifugeResponseDto response = this.centrifugeSchemaService.testRules(rules, event);
            ConfigEditorResult.StatusCode statusCode = ConfigEditorResult.StatusCode.ERROR;
            switch(response.getStatusCode()) {
                case OK:
                    exception.set(null);
                    attributes.setTestResultOutput(response.getAttributes().getMessage());
                    attributes.setTestResultComplete(true);
                    attributes.setTestResultRawOutput("{}"); //NOTE: test_result_raw_output not supported in centrifuge
                    return new ConfigEditorResult(OK, attributes);
                case BAD_REQUEST:
                    statusCode = ConfigEditorResult.StatusCode.BAD_REQUEST;
                    break;
            }

            attributes.setException(response.getAttributes().getException());
            attributes.setMessage(response.getAttributes().getMessage());
            return new ConfigEditorResult(statusCode, attributes);
        } catch (IOException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            exception.set(e);
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        String schema = this.centrifugeSchemaService.getTestSchema();
        ConfigEditorAttributes result = new ConfigEditorAttributes();
        result.setTestSchema(schema);
        return new ConfigEditorResult(OK, result);
    }

    @Override
    public Health checkHealth() {
        Exception current = exception.get();
        return current == null
                ? Health.up().build()
                : Health.down().withException(current).build();
    }
}
