package uk.co.gresearch.siembol.configeditor.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.ResponseTestSpecificationDto;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResponseSchemaService extends ConfigSchemaServiceAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter ATTRIBUTES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(RespondingResultAttributes.class);
    private static final String INIT_START_MESSAGE = "Response schema service initialisation started";
    private static final String INIT_COMPLETED_MESSAGE = "Response schema service initialisation completed";
    private static final String INIT_ERROR_MESSAGE = "Response schema service initialisation error";
    private static final String RULES_SCHEMA_LOG = "rules schema: {}";
    private static final String TEST_SPECIFICATION_SCHEMA_LOG = "test specification schema: {}";
    private static final String ATTRIBUTES_MISSING_ERROR = "Missing attributes in Response Schema Service";

    private final ResponseHttpProvider responseHttpProvider;

    ResponseSchemaService(Builder builder) {
        super(builder.context);
        responseHttpProvider = builder.responseHttpProvider;
    }

    @Override
    public ConfigEditorResult validateConfiguration(String configuration) {
        String rules = RespondingCompilerImpl.wrapRuleToRules(configuration);
        return validateConfigurations(rules);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configurations) {
        try {
            RespondingResult result  = responseHttpProvider.validateRules(configurations);
            return fromRespondingResult(result);
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    public static ConfigEditorResult fromRespondingResult(RespondingResult respondingResult) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        if (respondingResult.getStatusCode() != RespondingResult.StatusCode.OK) {
            attributes.setMessage(respondingResult.getAttributes().getMessage());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);
        }
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    public static class Builder {
        private ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        private ResponseHttpProvider responseHttpProvider;
        private HttpProvider httpProvider;
        private ConfigEditorUiLayout uiLayout = new ConfigEditorUiLayout();

        public Builder(HttpProvider httpProvider) {
            this.httpProvider = httpProvider;
        }

        public Builder uiConfigSchema(ConfigEditorUiLayout uiLayout) {
            this.uiLayout = uiLayout;
            return this;
        }

        public ResponseSchemaService build() throws Exception {
            responseHttpProvider = new ResponseHttpProvider(httpProvider);
            RespondingResult ruleSchemaResult = responseHttpProvider.getRulesSchema(uiLayout);
            var testSchemaValidator = new SiembolJsonSchemaValidator(ResponseTestSpecificationDto.class);
            String testSchema = testSchemaValidator.getJsonSchema().getAttributes().getJsonSchema();


            LOG.info(RULES_SCHEMA_LOG, ruleSchemaResult.getAttributes().getRulesSchema());
            if (ruleSchemaResult.getStatusCode() != RespondingResult.StatusCode.OK
                    || ruleSchemaResult.getAttributes().getRulesSchema() == null
                    || testSchema == null) {
                LOG.error(INIT_ERROR_MESSAGE);
                throw new IllegalStateException(INIT_ERROR_MESSAGE);
            }

            context.setConfigSchema(ruleSchemaResult.getAttributes().getRulesSchema());
            var responseConfigTester = new ResponseConfigTester(testSchemaValidator, testSchema, responseHttpProvider);
            context.setConfigTesters(List.of(responseConfigTester.withErrorMessage()));
            return new ResponseSchemaService(this);
        }
    }

    public static ConfigSchemaService createResponseSchemaService(ConfigEditorUiLayout uiConfig,
                                                                  Optional<Map<String, String>> attributes) throws Exception {
        LOG.info(INIT_START_MESSAGE);
        if (!attributes.isPresent()) {
            LOG.error(ATTRIBUTES_MISSING_ERROR);
            throw new IllegalArgumentException(ATTRIBUTES_MISSING_ERROR);
        }

        ResponseAttributes responseAttributes = new ObjectMapper()
                .convertValue(attributes.get(), ResponseAttributes.class);
        if (responseAttributes.getResponseUrl() == null
                || responseAttributes.getResponseAuthenticationType() == null) {
            LOG.error(ATTRIBUTES_MISSING_ERROR);
            throw new IllegalArgumentException(ATTRIBUTES_MISSING_ERROR);
        }

        HttpProvider httpProvider = new HttpProvider(responseAttributes.getResponseUrl(),
                responseAttributes.getResponseAuthenticationType().getHttpClientFactory());
        ConfigSchemaService ret = new Builder(httpProvider)
                .uiConfigSchema(uiConfig)
                .build();
        LOG.info(INIT_COMPLETED_MESSAGE);
        return ret;
    }
}
