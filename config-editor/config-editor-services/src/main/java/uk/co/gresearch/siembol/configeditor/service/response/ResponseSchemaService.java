package uk.co.gresearch.siembol.configeditor.service.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.response.common.InactiveMetricFactory;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorValidator;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;
import uk.co.gresearch.siembol.response.evaluators.arrayreducers.ArrayReducerEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.assignment.AssignmentEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.ArrayTableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.TableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.matching.MatchingEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.throttling.AlertThrottlingEvaluatorFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResponseSchemaService implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START_MESSAGE = "Response schema service initialisation started";
    private static final String INIT_COMPLETED_MESSAGE = "Response schema service initialisation completed";
    private static final String INIT_ERROR_MESSAGE = "Response schema service initialisation error";

    private final RespondingCompiler compiler;
    private final String rulesSchema;

    ResponseSchemaService(Builder builder) {
        rulesSchema = builder.rulesSchema;
        compiler = builder.compiler;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(rulesSchema);
    }

    @Override
    public ConfigEditorResult validateConfiguration(String configuration) {
        RespondingResult result = compiler.validateConfiguration(configuration);
        return fromRespondingResult(result);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configurations) {
        RespondingResult result = compiler.validateConfigurations(configurations);
        return fromRespondingResult(result);
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
        private String rulesSchema;
        private String uiConfigSchema;
        private RespondingCompiler compiler;
        private List<RespondingEvaluatorFactory> factories = new ArrayList<>();
        private List<RespondingEvaluatorValidator> validators = new ArrayList<>();
        private List<RespondingEvaluatorFactory> providedFactories;

        public Builder() throws Exception {
            providedFactories = Arrays.asList(new FixedEvaluatorFactory(),
                    new AssignmentEvaluatorFactory(),
                    new MatchingEvaluatorFactory(),
                    new TableFormatterEvaluatorFactory(),
                    new ArrayTableFormatterEvaluatorFactory(),
                    new AlertThrottlingEvaluatorFactory(),
                    new ArrayReducerEvaluatorFactory());
        }

        public Builder uiConfigSchema(String uiConfigSchema) {
            this.uiConfigSchema = uiConfigSchema;
            return this;
        }

        public Builder addRespondingEvaluatorFactory(RespondingEvaluatorFactory factory) {
            factories.add(factory);
            return this;
        }

        public Builder addRespondingEvaluatorValidator(RespondingEvaluatorValidator validator) {
            validators.add(validator);
            return this;
        }

        public Builder addRespondingEvaluatorFactories(List<RespondingEvaluatorFactory> factories) {
            this.factories.addAll(factories);
            return this;
        }

        public ResponseSchemaService build() throws Exception {
            LOG.info(INIT_START_MESSAGE);
            factories.addAll(providedFactories);
            RespondingCompilerImpl.Builder builder = new RespondingCompilerImpl.Builder()
                    .metricFactory(new InactiveMetricFactory())
                    .addRespondingEvaluatorFactories(factories);
            validators.forEach(x -> builder.addRespondingEvaluatorValidator(x));

            compiler = builder.build();
            String validationSchema = compiler.getSchema().getAttributes().getRulesSchema();
            rulesSchema = uiConfigSchema == null
                    ? validationSchema
                    : ConfigEditorUtils.computeRulesSchema(validationSchema, uiConfigSchema).get();
            if (rulesSchema == null) {
                LOG.error(INIT_ERROR_MESSAGE);
                throw new IllegalStateException(INIT_ERROR_MESSAGE);
            }

            LOG.info(INIT_COMPLETED_MESSAGE);
            return new ResponseSchemaService(this);
        }
    }
}
