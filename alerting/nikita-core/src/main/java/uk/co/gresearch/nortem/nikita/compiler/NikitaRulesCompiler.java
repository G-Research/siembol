package uk.co.gresearch.nortem.nikita.compiler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.testing.StringTestingLogger;
import uk.co.gresearch.nortem.common.testing.TestingLogger;
import uk.co.gresearch.nortem.nikita.common.*;
import uk.co.gresearch.nortem.nikita.engine.*;
import uk.co.gresearch.nortem.nikita.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static uk.co.gresearch.nortem.nikita.common.NikitaResult.StatusCode.OK;

public class NikitaRulesCompiler implements NikitaCompiler {
    private static final ObjectReader JSON_RULES_READER =
            new ObjectMapper().readerFor(RulesDto.class);
    private static final ObjectWriter JSON_RULES_WRITER =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerFor(RulesDto.class);
    private static final ObjectReader JSON_RULE_READER =
            new ObjectMapper().readerFor(RuleDto.class);
    private static final String TEST_FIELD_NAME = "nikita:test";
    private static final String TEST_FIELD_VALUE = "true";
    private static final String TESTING_START_MSG = "Start testing on the event: %s";
    private static final String TESTING_FINISHED_MSG = "The testing finished with the result: %s";
    private static final String OUTPUT_EVENTS_MSG = "Output events:";
    private static final String EXCEPTION_EVENTS_MSG = "Exception events:";

    private final JsonSchemaValidator jsonSchemaValidator;
    private final List<TagsDto> testOutputContants;

    NikitaRulesCompiler(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;

        final TagsDto testConstant = new TagsDto();
        testConstant.setTagName(TEST_FIELD_NAME);
        testConstant.setTagValue(TEST_FIELD_VALUE);
        this.testOutputContants = Arrays.asList(testConstant);
    }

    private RuleMatcher createMatcher(MatcherDto matcherDto) {
        switch (MatcherType.valueOf(matcherDto.getType())){
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
        }
        throw new IllegalArgumentException("Unknown matcher type");
    }

    @Override
    public NikitaResult compile(String rules, TestingLogger logger) {
        NikitaResult validateSchemaResult = validateRulesSyntax(rules);
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
                generalProtections.add(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName(),
                        rulesDto.getRulesProtection().getMaxPerHour()));
                generalProtections.add(Pair.of(NikitaFields.MAX_PER_DAY_FIELD.getNikitaName(),
                        rulesDto.getRulesProtection().getMaxPerDay()));
            }

            List<Pair<String, Rule>> rulesList = new ArrayList<>();
            for (RuleDto ruleDto : rulesDto.getRules()) {
                List<RuleMatcher> matchers = ruleDto.getMatchers()
                        .stream()
                        .map(x -> createMatcher(x))
                        .collect(Collectors.toList());

                List<Pair<String, String>> constants = ruleDto.getTags() != null
                        ? ruleDto.getTags()
                        .stream()
                        .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                        .collect(Collectors.toList()) : new ArrayList<>();

                List<Pair<String, Object>> protections = new ArrayList<>();
                if (ruleDto.getRuleProtection() != null) {
                    protections.add(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName(),
                            ruleDto.getRuleProtection().getMaxPerHour()));
                    protections.add(Pair.of(NikitaFields.MAX_PER_DAY_FIELD.getNikitaName(),
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

            NikitaEngine engine = new NikitaEngineImpl.Builder()
                    .constants(generalConstants)
                    .protections(generalProtections)
                    .rules(rulesList)
                    .build();

            NikitaAttributes attributes = new NikitaAttributes();
            attributes.setEngine(engine);
            return new NikitaResult(OK, attributes);
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
    }

    @Override
    public JsonSchemaValidator getSchemaValidator() {
        return jsonSchemaValidator;
    }

    @Override
    public NikitaResult testRules(String rules, String event) {
        TestingLogger logger = new StringTestingLogger();
        NikitaResult compileResult = compile(rules, logger);
        if (compileResult.getStatusCode() != OK) {
            return compileResult;
        }

        logger.appendMessage(String.format(TESTING_START_MSG, event));
        NikitaEngine engine = compileResult.getAttributes().getEngine();
        NikitaResult testResult = engine.evaluate(event);
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

        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setMessage(logger.getLog());
        return new NikitaResult(NikitaResult.StatusCode.OK, attributes);
    }

    @Override
    public String wrapRuleToRules(String ruleStr) throws IOException {
        RuleDto rule = JSON_RULE_READER.readValue(ruleStr);
        RulesDto rules = new RulesDto();
        rules.setTags(testOutputContants);
        rules.setRulesVersion(rule.getRuleVersion());
        rules.setRules(Arrays.asList(rule));
        return JSON_RULES_WRITER.writeValueAsString(rules);
    }

    public static NikitaCompiler createNikitaRulesCompiler() throws Exception {
        JsonSchemaValidator validator = new NortemJsonSchemaValidator(RulesDto.class);
        return new NikitaRulesCompiler(validator);
    }
}
