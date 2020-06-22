package uk.co.gresearch.siembol.response.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.UnionJsonType;
import uk.co.gresearch.siembol.common.jsonschema.UnionJsonTypeOption;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.common.testing.StringTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.engine.ResponseEngine;
import uk.co.gresearch.siembol.response.engine.ResponseRule;
import uk.co.gresearch.siembol.response.engine.RulesEngine;
import uk.co.gresearch.siembol.response.model.ResponseEvaluatorDto;
import uk.co.gresearch.siembol.response.model.ResponseTestSpecificationDto;
import uk.co.gresearch.siembol.response.model.RuleDto;
import uk.co.gresearch.siembol.response.model.RulesDto;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public class RespondingCompilerImpl implements RespondingCompiler {
    private static ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(ResponseTestSpecificationDto.class);
    private static final String TESTING_START_MSG = "Start testing on the event: %s";
    private static final String TESTING_FINISHED_MSG = "The testing finished with the status: %s";
    private static final String TEST_ALERT_ID_FORMAT_MSG = "test-%s";
    private static final String ERROR_FORMAT_MSG = "error message: %s";
    private static final String ALERT_FORMAT_MSG = "result:%s alert: %s";

    private static final String EVALUATOR_TITLE = "response evaluator";
    private static final ObjectReader RULES_READER = new ObjectMapper().readerFor(RulesDto.class);
    private static final String RULES_WRAP_MSG = "{\"rules_version\":1, \"rules\":[%s]}";
    private static final String UNSUPPORTED_EVALUATOR_TYPE_MSG = "Unsupported response evaluator type %s";
    private final Map<String, RespondingEvaluatorFactory> respondingEvaluatorFactoriesMap;
    private final String rulesJsonSchemaStr;
    private final JsonSchemaValidator rulesSchemaValidator;
    private final JsonSchemaValidator testSpecificationValidator;
    private final MetricFactory metricFactory;

    public RespondingCompilerImpl(Builder builder) {
        this.respondingEvaluatorFactoriesMap = builder.respondingEvaluatorFactoriesMap;
        this.rulesJsonSchemaStr = builder.rulesJsonSchemaStr;
        this.rulesSchemaValidator = builder.rulesSchemaValidator;
        this.metricFactory = builder.metricFactory;
        this.testSpecificationValidator = builder.testSpecificationValidator;
    }

    private ResponseRule createResponseRule(RuleDto ruleDto, TestingLogger logger) {
        ResponseRule.Builder builder = new ResponseRule.Builder();
        builder
                .metricFactory(metricFactory)
                .logger(logger)
                .ruleName(ruleDto.getRuleName())
                .ruleVersion(ruleDto.getRuleVersion());

        for (ResponseEvaluatorDto evaluatorDto : ruleDto.getEvaluators()) {
            String evaluatorType = evaluatorDto.getEvaluatorType();
            if (!respondingEvaluatorFactoriesMap.containsKey(evaluatorType)) {
                throw new IllegalArgumentException(String.format(
                        UNSUPPORTED_EVALUATOR_TYPE_MSG, evaluatorType));
            }
            RespondingResult evaluatorResult = respondingEvaluatorFactoriesMap.get(evaluatorType)
                    .createInstance(evaluatorDto.getEvaluatorAttributesContent());
            if (evaluatorResult.getStatusCode() != OK) {
                throw new IllegalArgumentException(evaluatorResult.getAttributes().getMessage());
            }
            builder.addEvaluator(evaluatorResult.getAttributes().getRespondingEvaluator());
        }
        return builder.build();
    }

    @Override
    public RespondingResult compile(String rules, TestingLogger logger) {
        RespondingResult validationResult = validateConfigurations(rules);
        if (validationResult.getStatusCode() != OK) {
            return validationResult;
        }

        try {
            RulesDto rulesDto = RULES_READER.readValue(rules);
            List<ResponseRule> responseRules = rulesDto.getRules().stream()
                    .map(x -> createResponseRule(x, logger))
                    .collect(Collectors.toList());

            RespondingResultAttributes metadataAttributes = new RespondingResultAttributes();
            metadataAttributes.setRulesVersion(rulesDto.getRulesVersion());
            metadataAttributes.setJsonRules(rules);
            metadataAttributes.setCompiledTime(System.currentTimeMillis());
            metadataAttributes.setNumberOfRules(responseRules.size());

            ResponseEngine responseEngine = new RulesEngine.Builder()
                    .metadata(metadataAttributes)
                    .rules(responseRules)
                    .metricFactory(metricFactory)
                    .testingLogger(logger)
                    .build();

            RespondingResultAttributes attr = new RespondingResultAttributes();
            attr.setResponseEngine(responseEngine);
            return new RespondingResult(OK, attr);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getSchema() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRulesSchema(rulesJsonSchemaStr);
        return new RespondingResult(OK, attributes);
    }

    @Override
    public RespondingResult getTestSpecificationSchema() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setTestSpecificationSchema(testSpecificationValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new RespondingResult(OK, attributes);
    }

    @Override
    public RespondingResult testConfigurations(String rules, String testSpecification) {
        SiembolResult validationResult = testSpecificationValidator.validate(testSpecification);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
            return RespondingResult.fromSiembolResult(validationResult);
        }
        try {
            ResponseTestSpecificationDto testSpecificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
            String alertId = String.format(TEST_ALERT_ID_FORMAT_MSG, UUID.randomUUID().toString());
            ResponseAlert responseAlert = ResponseAlert.fromOriginalString(alertId,
                    testSpecificationDto.getEventContent());

            TestingLogger logger = new StringTestingLogger();

            logger.appendMessage(String.format(TESTING_START_MSG, responseAlert.toString()));
            RespondingResult rulesEngineResult = compile(rules, logger);
            if (rulesEngineResult.getStatusCode() != OK) {
                return rulesEngineResult;
            }

            RespondingResult result = rulesEngineResult.getAttributes().getResponseEngine()
                    .evaluate(responseAlert);

            logger.appendMessage(String.format(TESTING_FINISHED_MSG, result.getStatusCode()));
            if (result.getStatusCode() != OK) {
                logger.appendMessage(String.format(ERROR_FORMAT_MSG, result.getAttributes().getMessage()));
            } else {
                logger.appendMessage(String.format(ALERT_FORMAT_MSG,
                        result.getAttributes().getResult().toString(),
                        result.getAttributes().getAlert().toString()));
            }

            RespondingResultAttributes returnAttributes = new RespondingResultAttributes();
            returnAttributes.setMessage(logger.getLog());
            return new RespondingResult(OK, returnAttributes);
        }
        catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult validateConfiguration(String rule) {
        try {
            return validateConfigurations(wrapRuleToRules(rule));
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getRespondingEvaluatorFactories() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorFactories(respondingEvaluatorFactoriesMap.values().stream()
                .collect(Collectors.toList()));
        return new RespondingResult(OK, attributes);
    }

    public static String wrapRuleToRules(String ruleStr) {
        return String.format(RULES_WRAP_MSG, ruleStr);
    }

    @Override
    public RespondingResult validateConfigurations(String rules) {
        SiembolResult validationResult = rulesSchemaValidator.validate(rules);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
            return RespondingResult.fromSiembolResult(validationResult);
        }

        try {
            RulesDto rulesDto = RULES_READER.readValue(rules);
            for (RuleDto ruleDto : rulesDto.getRules()) {
                for (ResponseEvaluatorDto evaluatorDto : ruleDto.getEvaluators()) {
                    String evaluatorType = evaluatorDto.getEvaluatorType();
                    if (!respondingEvaluatorFactoriesMap.containsKey(evaluatorType)) {
                        throw new IllegalArgumentException(String.format(
                                UNSUPPORTED_EVALUATOR_TYPE_MSG, evaluatorType));
                    }
                    RespondingResult validationAttributesResult = respondingEvaluatorFactoriesMap.get(evaluatorType)
                            .validateAttributes(evaluatorDto.getEvaluatorAttributesContent());
                    if (validationAttributesResult.getStatusCode() != OK) {
                        return validationAttributesResult;
                    }
                }
            }
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
        return new RespondingResult(OK, new RespondingResultAttributes());
    }

    public static class Builder {
        private static final String EVALUATOR_DUPLICATE_TYPE = "Evaluator type: %s already registered";
        private static final String EMPTY_EVALUATORS = "Response evaluators are empty";
        private Map<String, RespondingEvaluatorFactory> respondingEvaluatorFactoriesMap = new HashMap<>();
        private String rulesJsonSchemaStr;
        private JsonSchemaValidator rulesSchemaValidator;
        private JsonSchemaValidator testSpecificationValidator;
        private MetricFactory metricFactory;

        public Builder metricFactory(MetricFactory metricFactory) {
            this.metricFactory = metricFactory;
            return this;
        }

        public Builder addRespondingEvaluatorFactories(List<RespondingEvaluatorFactory> factories) {
            factories.forEach(this::addRespondingEvaluatorFactory);
            return this;
        }

        public Builder addRespondingEvaluatorFactory(RespondingEvaluatorFactory factory) {
            if (respondingEvaluatorFactoriesMap.containsKey(factory.getType().getAttributes().getEvaluatorType())) {
                throw new IllegalArgumentException(String.format(EVALUATOR_DUPLICATE_TYPE, factory.getType()));
            }

            respondingEvaluatorFactoriesMap.put(factory.getType().getAttributes().getEvaluatorType(), factory);
            return this;
        }

        public RespondingCompilerImpl build() throws Exception {
            if (respondingEvaluatorFactoriesMap.isEmpty()) {
                throw new IllegalArgumentException(EMPTY_EVALUATORS);
            }

            testSpecificationValidator = new SiembolJsonSchemaValidator(ResponseTestSpecificationDto.class);

            respondingEvaluatorFactoriesMap.forEach((k, v) -> {
                v.registerMetrics(metricFactory);
            });

            List<UnionJsonTypeOption> evaluatorOptions = respondingEvaluatorFactoriesMap.keySet().stream()
                    .map(x ->
                            new UnionJsonTypeOption(
                                    respondingEvaluatorFactoriesMap.get(x).getType()
                                            .getAttributes().getEvaluatorType(),
                                    respondingEvaluatorFactoriesMap.get(x).getAttributesJsonSchema()
                                            .getAttributes().getAttributesSchema()))
                    .collect(Collectors.toList());

            evaluatorOptions.sort(Comparator.comparing(x -> x.getSelectorName()));
            UnionJsonType options = new UnionJsonType(EVALUATOR_TITLE, evaluatorOptions);
            rulesSchemaValidator = new SiembolJsonSchemaValidator(RulesDto.class, Optional.of(Arrays.asList(options)));
            rulesJsonSchemaStr = rulesSchemaValidator.getJsonSchema().getAttributes().getJsonSchema();
            return new RespondingCompilerImpl(this);
        }
    }
}

