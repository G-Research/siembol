package uk.co.gresearch.nortem.configeditor.service.nikita;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCompiler;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCorrelationRulesCompiler;
import uk.co.gresearch.nortem.nikita.compiler.NikitaRulesCompiler;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class NikitaRuleSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final String SCHEMA_INIT_ERROR = "Error during computing rules schema";
    private static final String TESTING_ERROR = "Unexpected rule testing service result";
    private final NikitaCompiler nikitaCompiler;
    private final String schema;

    NikitaRuleSchemaServiceImpl(NikitaCompiler nikitaCompiler, String schema) {
        this.nikitaCompiler = nikitaCompiler;
        this.schema = schema;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(schema);
    }

    @Override
    public ConfigEditorResult validateConfiguration(String rule) {
        NikitaResult nikitaResult = nikitaCompiler.validateRule(rule);
        return fromNikitaValidateResult(nikitaResult);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String rules) {
        NikitaResult nikitaResult = nikitaCompiler.validateRules(rules);
        return fromNikitaValidateResult(nikitaResult);
    }

    public static ConfigSchemaService createNikitaRuleSchema(Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising nikita rule schema service");
        NikitaCompiler compiler = NikitaRulesCompiler.createNikitaRulesCompiler();
        NikitaResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != NikitaResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || !uiConfig.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .computeRulesSchema(schemaResult.getAttributes().getRulesSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        LOG.info("Initialising nikita rule schema service completed");
        return new NikitaRuleSchemaServiceImpl(compiler, computedSchema.get());
    }

    public static ConfigSchemaService createNikitaCorrelationRuleSchema(
            Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising nikita correlation rule schema service");
        NikitaCompiler compiler = NikitaCorrelationRulesCompiler.createNikitaCorrelationRulesCompiler();
        NikitaResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != NikitaResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || !uiConfig.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .computeRulesSchema(schemaResult.getAttributes().getRulesSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        LOG.info("Initialising nikita correlation rule schema service completed");
        return new NikitaRuleSchemaServiceImpl(compiler, computedSchema.get());
    }

    @Override
    public ConfigEditorResult testConfiguration(String rule, String event) {
        return fromNikitaTestResult(nikitaCompiler.testRule(rule, event));
    }

    @Override
    public ConfigEditorResult testConfigurations(String rule, String event) {
        return fromNikitaTestResult(nikitaCompiler.testRules(rule, event));
    }

    private ConfigEditorResult fromNikitaValidateResult(NikitaResult nikitaResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = nikitaResult.getStatusCode() == NikitaResult.StatusCode.OK
                ? ConfigEditorResult.StatusCode.OK
                : ConfigEditorResult.StatusCode.ERROR;

        attr.setMessage(nikitaResult.getAttributes().getMessage());
        attr.setException(nikitaResult.getAttributes().getException());

        return new ConfigEditorResult(statusCode, attr);
    }

    private ConfigEditorResult fromNikitaTestResult(NikitaResult nikitaResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (nikitaResult.getStatusCode() != NikitaResult.StatusCode.OK) {
            attr.setMessage(nikitaResult.getAttributes().getMessage());
            attr.setException(nikitaResult.getAttributes().getException());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.ERROR, attr);
        }

        if (nikitaResult.getAttributes().getMessage() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    TESTING_ERROR);
        }

        attr.setTestResultComplete(true);
        attr.setTestResultOutput(nikitaResult.getAttributes().getMessage());
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }
}
