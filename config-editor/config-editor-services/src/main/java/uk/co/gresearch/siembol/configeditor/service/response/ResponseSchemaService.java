package uk.co.gresearch.siembol.configeditor.service.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class ResponseSchemaService implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START_MESSAGE = "Response schema service initialisation started";
    private static final String INIT_COMPLETED_MESSAGE = "Response schema service initialisation completed";
    private static final String INIT_ERROR_MESSAGE = "Response schema service initialisation error";

    private final ResponseHttpProvider responseHttpProvider;
    private final String rulesSchema;
    private final String testSchema;

    ResponseSchemaService(Builder builder) {
        rulesSchema = builder.rulesSchema;
        testSchema = builder.testSchema;
        responseHttpProvider = builder.responseHttpProvider;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(rulesSchema);
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTestSchema(testSchema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
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

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        String rules = RespondingCompilerImpl.wrapRuleToRules(configuration);
        return testConfigurations(rules, testSpecification);
    }

    @Override
    public ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        try {
            RespondingResult result = responseHttpProvider.testRules(configurations, testSpecification);
            return fromRespondingResult(result);
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    private ConfigEditorResult fromRespondingResult(RespondingResult respondingResult) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        if (respondingResult.getStatusCode() != RespondingResult.StatusCode.OK) {
            attributes.setMessage(respondingResult.getAttributes().getMessage());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);
        }
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    public static class Builder {
        private ResponseHttpProvider responseHttpProvider;
        private HttpProvider httpProvider;
        private String rulesSchema;
        private String testSchema;
        private String uiConfigSchema;
        private String uiConfigTestSchema;

        public Builder(HttpProvider httpProvider) {
            this.httpProvider = httpProvider;
        }

        public Builder uiConfigSchema(String uiConfigSchema) {
            this.uiConfigSchema = uiConfigSchema;
            return this;
        }

        public Builder uiConfigTestSchema(String uiConfigTestSchema) {
            this.uiConfigTestSchema = uiConfigTestSchema;
            return this;
        }

        public ResponseSchemaService build() throws Exception {
            LOG.info(INIT_START_MESSAGE);
            responseHttpProvider = new ResponseHttpProvider(httpProvider);

            RespondingResult ruleSchemaResult = responseHttpProvider
                    .getRulesSchema(Optional.ofNullable(uiConfigSchema));
            RespondingResult testSchemaResult = responseHttpProvider
                    .getTestSchema(Optional.ofNullable(uiConfigTestSchema));

            LOG.info(ruleSchemaResult.getAttributes().getRulesSchema());
            LOG.info(testSchemaResult.getAttributes().getTestSpecificationSchema());
            if (ruleSchemaResult.getStatusCode() != RespondingResult.StatusCode.OK
                    || ruleSchemaResult.getAttributes().getRulesSchema() == null
                    || testSchemaResult.getStatusCode() != RespondingResult.StatusCode.OK
                    || testSchemaResult.getAttributes().getTestSpecificationSchema() == null) {
                LOG.error(INIT_ERROR_MESSAGE);
                throw new IllegalStateException(INIT_ERROR_MESSAGE);
            }

            rulesSchema = ruleSchemaResult.getAttributes().getRulesSchema();
            testSchema = testSchemaResult.getAttributes().getTestSpecificationSchema();
            LOG.info(INIT_COMPLETED_MESSAGE);
            return new ResponseSchemaService(this);
        }
    }
}
