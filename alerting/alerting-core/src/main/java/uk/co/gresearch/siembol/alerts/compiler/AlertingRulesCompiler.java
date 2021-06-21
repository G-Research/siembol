package uk.co.gresearch.siembol.alerts.compiler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.testing.StringTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.alerts.common.*;
import uk.co.gresearch.siembol.alerts.engine.*;
import uk.co.gresearch.siembol.alerts.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;

public class AlertingRulesCompiler implements AlertingCompiler {
    private static final ObjectReader JSON_RULES_READER =
            new ObjectMapper().readerFor(RulesDto.class);
    private static final ObjectWriter JSON_RULES_WRITER =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerFor(RulesDto.class);
    private static final ObjectReader JSON_RULE_READER =
            new ObjectMapper().readerFor(RuleDto.class);
    private static final String TEST_FIELD_NAME = "alerts:test";
    private static final String TEST_FIELD_VALUE = "true";
    private static final String TESTING_START_MSG = "Start testing on the event: %s";
    private static final String TESTING_FINISHED_MSG = "The testing finished with the result: %s";
    private static final String OUTPUT_EVENTS_MSG = "Output events:";
    private static final String EXCEPTION_EVENTS_MSG = "Exception events:";
    private static final String UNSUPPORTED_MATCHER_TYPE = "Unsupported matcher type: %s";
    private static final String MISSING_MATCHERS_IN_COMPOSITE_MATCHER = "Missing matchers in a composite matcher";

    private final JsonSchemaValidator jsonSchemaValidator;
    private final List<TagDto> testOutputConstants;

    AlertingRulesCompiler(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;

        final TagDto testConstant = new TagDto();
        testConstant.setTagName(TEST_FIELD_NAME);
        testConstant.setTagValue(TEST_FIELD_VALUE);
        this.testOutputConstants = Collections.singletonList(testConstant);
    }

    private Matcher createMatcher(MatcherDto matcherDto) {
        MatcherType matcherType = MatcherType.valueOf(matcherDto.getType().toString());
        switch (matcherType) {
            case REGEX_MATCH:
                return RegexMatcher.builder()
                        .pattern(matcherDto.getData())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
            case IS_IN_SET:
                return IsInSetMatcher.builder()
                        .data(matcherDto.getData())
                        .isCaseInsensitiveCompare(matcherDto.getCaseInsensitiveCompare())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
            case COMPOSITE_AND:
            case COMPOSITE_OR:
                if (matcherDto.getMatchers() == null) {
                    throw new IllegalArgumentException(MISSING_MATCHERS_IN_COMPOSITE_MATCHER);
                }
                List<Matcher> matchers = matcherDto.getMatchers().stream()
                        .map(this::createMatcher)
                        .collect(Collectors.toList());
                return CompositeMatcher.builder()
                        .matcherType(matcherType)
                        .matchers(matchers)
                        .isNegated(matcherDto.getNegated())
                        .build();
            default:
                throw new IllegalArgumentException(String.format(UNSUPPORTED_MATCHER_TYPE,
                        matcherDto.getType().toString()));
        }

    }

    @Override
    public AlertingResult compile(String rules, TestingLogger logger) {
        AlertingResult validateSchemaResult = validateRulesSyntax(rules);
        if (validateSchemaResult.getStatusCode() != OK) {
            return validateSchemaResult;
        }

        try {
            RulesDto rulesDto = JSON_RULES_READER.readValue(rules);
            List<Pair<String, String>> generalConstants = rulesDto.getTags() != null
                    ? rulesDto.getTags()
                    .stream()
                    .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                    .collect(Collectors.toList()) : new ArrayList<>();

            List<Pair<String, Object>> generalProtections = new ArrayList<>();
            if (rulesDto.getRulesProtection() != null) {
                generalProtections.add(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName(),
                        rulesDto.getRulesProtection().getMaxPerHour()));
                generalProtections.add(Pair.of(AlertingFields.MAX_PER_DAY_FIELD.getAlertingName(),
                        rulesDto.getRulesProtection().getMaxPerDay()));
            }

            List<Pair<String, Rule>> rulesList = new ArrayList<>();
            for (RuleDto ruleDto : rulesDto.getRules()) {
                List<Matcher> matchers = ruleDto.getMatchers()
                        .stream()
                        .map(this::createMatcher)
                        .collect(Collectors.toList());

                List<Pair<String, String>> constants = ruleDto.getTags() != null
                        ? ruleDto.getTags()
                        .stream()
                        .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                        .collect(Collectors.toList()) : new ArrayList<>();

                List<Pair<String, Object>> protections = new ArrayList<>();
                if (ruleDto.getRuleProtection() != null) {
                    protections.add(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName(),
                            ruleDto.getRuleProtection().getMaxPerHour()));
                    protections.add(Pair.of(AlertingFields.MAX_PER_DAY_FIELD.getAlertingName(),
                            ruleDto.getRuleProtection().getMaxPerDay()));
                }

                Rule current = Rule.builder()
                        .matchers(matchers)
                        .name(ruleDto.getRuleName())
                        .version(ruleDto.getRuleVersion())
                        .tags(constants)
                        .protections(protections)
                        .logger(logger)
                        .build();

                rulesList.add(Pair.of(ruleDto.getSourceType(), current));
            }

            AlertingEngine engine = new AlertingEngineImpl.Builder()
                    .constants(generalConstants)
                    .protections(generalProtections)
                    .rules(rulesList)
                    .build();

            AlertingAttributes attributes = new AlertingAttributes();
            attributes.setEngine(engine);
            return new AlertingResult(OK, attributes);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    @Override
    public JsonSchemaValidator getSchemaValidator() {
        return jsonSchemaValidator;
    }

    @Override
    public AlertingResult testRules(String rules, String event) {
        TestingLogger logger = new StringTestingLogger();
        AlertingResult compileResult = compile(rules, logger);
        if (compileResult.getStatusCode() != OK) {
            return compileResult;
        }

        logger.appendMessage(String.format(TESTING_START_MSG, event));
        AlertingEngine engine = compileResult.getAttributes().getEngine();
        AlertingResult testResult = engine.evaluate(event);
        if (testResult.getStatusCode() != OK) {
            return testResult;
        }

        logger.appendMessage(String.format(TESTING_FINISHED_MSG,
                testResult.getAttributes().getEvaluationResult().toString()));

        if (testResult.getAttributes().getOutputEvents() != null) {
            logger.appendMessage(OUTPUT_EVENTS_MSG);
            for (Map<String, Object> outputEvent : testResult.getAttributes().getOutputEvents()) {
                logger.appendMap(outputEvent);
            }
        }

        if (testResult.getAttributes().getExceptionEvents() != null) {
            logger.appendMessage(EXCEPTION_EVENTS_MSG);
            for (Map<String, Object> exceptionEvent : testResult.getAttributes().getExceptionEvents()) {
                logger.appendMap(exceptionEvent);
            }
        }

        testResult.getAttributes().setMessage(logger.getLog());
        return testResult;
    }

    @Override
    public String wrapRuleToRules(String ruleStr) throws IOException {
        RuleDto rule = JSON_RULE_READER.readValue(ruleStr);
        RulesDto rules = new RulesDto();
        rules.setTags(testOutputConstants);
        rules.setRulesVersion(rule.getRuleVersion());
        rules.setRules(Collections.singletonList(rule));
        return JSON_RULES_WRITER.writeValueAsString(rules);
    }

    public static AlertingCompiler createAlertingRulesCompiler() throws Exception {
        JsonSchemaValidator validator = new SiembolJsonSchemaValidator(RulesDto.class);
        return new AlertingRulesCompiler(validator);
    }
}
