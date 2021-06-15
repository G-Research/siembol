package uk.co.gresearch.siembol.enrichments.compiler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.common.testing.StringTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.enrichments.evaluation.EnrichingRule;
import uk.co.gresearch.siembol.enrichments.evaluation.EnrichmentEvaluator;
import uk.co.gresearch.siembol.enrichments.evaluation.EnrichmentEvaluatorLibrary;
import uk.co.gresearch.siembol.enrichments.evaluation.AlertingEnrichmentEvaluator;
import uk.co.gresearch.siembol.enrichments.model.*;
import uk.co.gresearch.siembol.enrichments.table.EnrichmentMemoryTable;
import uk.co.gresearch.siembol.enrichments.table.EnrichmentTable;
import uk.co.gresearch.siembol.alerts.engine.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.OK;

public class EnrichmentCompilerImpl implements EnrichmentCompiler {
    private static final String TEST_START_MESSAGE = "Starting testing enriching rules:\n%s\n, " +
            "with test specification:\n%s";
    private static final String TEST_EMPTY_ENRICHMENTS_COMMANDS = "Nothing to enrich";
    private static final String TEST_ENRICHING_COMMANDS_MESSAGE = "Trying to apply enriching commands:";
    private static final String TEST_ENRICHED_FIELDS = "Enriching fields of the command:";
    private static final String TEST_TAG_FIELDS = "Tags of the command:";
    private static final String TEST_ENRICHED_MESSAGES = "Enriched pairs added to the event:";
    private static final String TEST_ENRICHED_EVENT = "Enriched event:";
    private static final String UNSUPPORTED_MATCHER = "Unsupported matcher %s in enrichments rules";
    private static final String RULE_TAGS_ENRICHMENTS_EMPTY_MSG = "Both enriching fields and tags are empty";

    private static final ObjectReader JSON_RULES_READER = new ObjectMapper().readerFor(RulesDto.class);
    private static final ObjectReader JSON_RULE_READER = new ObjectMapper().readerFor(RuleDto.class);
    private static final ObjectWriter JSON_RULES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(RulesDto.class);
    private static final ObjectReader JSON_TEST_SPEC_READER = new ObjectMapper()
            .readerFor(TestingSpecificationDto.class);

    private final JsonSchemaValidator rulesSchemaValidator;
    private final JsonSchemaValidator testSchemaValidator;

    EnrichmentCompilerImpl(JsonSchemaValidator rulesSchemaValidator, JsonSchemaValidator testSchemaValidator) {
        this.rulesSchemaValidator = rulesSchemaValidator;
        this.testSchemaValidator = testSchemaValidator;
    }

    private BasicMatcher createMatcher(MatcherDto matcherDto) {
        switch (matcherDto.getType()) {
            case REGEX_MATCH:
                return RegexMatcher
                        .builder()
                        .pattern(matcherDto.getData())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
            case IS_IN_SET:
                return IsInSetMatcher
                        .builder()
                        .data(matcherDto.getData())
                        .isCaseInsensitiveCompare(matcherDto.getCaseInsensitiveCompare())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
        }

        throw new UnsupportedOperationException(String.format(UNSUPPORTED_MATCHER, matcherDto.getType().toString()));
    }

    private Pair<String, Rule> createAlertingRule(RuleDto ruleDto) {
        if (ruleDto.getTableMapping().getEnrichingFields() == null
                && ruleDto.getTableMapping().getTags() == null) {
            throw new IllegalArgumentException(RULE_TAGS_ENRICHMENTS_EMPTY_MSG);
        }

        List<Matcher> matchers = ruleDto.getMatchers().stream()
                .map(x -> createMatcher(x))
                .collect(Collectors.toList());

        List<Pair<String, String>> enrichingFields = ruleDto.getTableMapping().getEnrichingFields() != null
                ? ruleDto.getTableMapping().getEnrichingFields().stream()
                .map(x -> Pair.of(x.getTableFieldName(), x.getEventFieldName()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<Pair<String, String>> enrichingTags = ruleDto.getTableMapping().getTags() != null
                ? ruleDto.getTableMapping().getTags().stream()
                .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        EnrichingRule rule = EnrichingRule.enrichingRuleBuilder()
                .key(ruleDto.getTableMapping().getJoiningKey())
                .tableName(ruleDto.getTableMapping().getTableName())
                .enrichmentTags(enrichingTags)
                .enrichmentFields(enrichingFields)
                .matchers(matchers)
                .name(ruleDto.getRuleName())
                .version(ruleDto.getRuleVersion())
                .build();

        return Pair.of(ruleDto.getSourceType(), rule);
    }

    private Map<String, EnrichmentTable> createTestingTable(TestingSpecificationDto test) throws IOException {
        Map<String, EnrichmentTable> ret = new HashMap<>();
        try (InputStream is = new ByteArrayInputStream(test.getTestingTableMappingContent().getBytes())) {
            EnrichmentMemoryTable current = EnrichmentMemoryTable.fromJsonStream(is);
            ret.put(test.getTestingTableName(), current);
        }
        return ret;
    }

    @Override
    public EnrichmentResult compile(String rules, TestingLogger logger) {
        SiembolResult validationResult = rulesSchemaValidator.validate(rules);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
            return EnrichmentResult.fromSiembolResult(validationResult);
        }

        try {
            RulesDto rulesDto = JSON_RULES_READER.readValue(rules);

            List<Pair<String, Rule>> alertingRules = rulesDto.getRules().stream()
                    .map(this::createAlertingRule)
                    .collect(Collectors.toList());

            EnrichmentEvaluator ruleEvaluator = new AlertingEnrichmentEvaluator.Builder()
                    .rules(alertingRules)
                    .build();

            EnrichmentAttributes attr = new EnrichmentAttributes();
            attr.setRuleEvaluator(ruleEvaluator);
            return new EnrichmentResult(OK, attr);
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }
    }

    @Override
    public EnrichmentResult getSchema() {
        EnrichmentAttributes attr = new EnrichmentAttributes();
        attr.setRulesSchema(rulesSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new EnrichmentResult(OK, attr);
    }

    @Override
    public EnrichmentResult getTestSpecificationSchema() {
        EnrichmentAttributes attr = new EnrichmentAttributes();
        attr.setTestSchema(testSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new EnrichmentResult(OK, attr);
    }

    @Override
    public EnrichmentResult validateConfiguration(String configuration) {
        try {
            return validateConfigurations(wrapRuleToRules(configuration));
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }
    }

    @Override
    public EnrichmentResult testConfiguration(String configuration, String testSpecification) {
        try {
            return testConfigurations(wrapRuleToRules(configuration), testSpecification);
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }
    }

    private void logEnrichmentCommands(TestingLogger logger, EnrichmentCommand command) {
        if (command.getTags() != null) {
            logger.appendMessage(TEST_TAG_FIELDS);
            command.getTags().forEach(x -> logger.appendMessage(x.toString()));
        }

        if (command.getEnrichmentFields() != null) {
            logger.appendMessage(TEST_ENRICHED_FIELDS);
            command.getEnrichmentFields().forEach(x -> logger.appendMessage(x.toString()));
        }
    }

    @Override
    public EnrichmentResult testConfigurations(String rules, String testSpecification) {
        EnrichmentAttributes attributes = new EnrichmentAttributes();

        SiembolResult validationResult = testSchemaValidator.validate(testSpecification);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
            return EnrichmentResult.fromSiembolResult(validationResult);
        }

        TestingLogger logger = new StringTestingLogger();
        EnrichmentResult evaluatorResult = compile(rules, logger);
        if (evaluatorResult.getStatusCode() != OK) {
            return evaluatorResult;
        }

        EnrichmentEvaluator evaluator = evaluatorResult.getAttributes().getRuleEvaluator();
        logger.appendMessage(String.format(TEST_START_MESSAGE, rules, testSpecification));

        try {
            TestingSpecificationDto specification = JSON_TEST_SPEC_READER.readValue(testSpecification);
            Map<String, EnrichmentTable> tables = createTestingTable(specification);

            EnrichmentResult result = evaluator.evaluate(specification.getEventContent());
            if(result.getAttributes().getEnrichmentCommands() == null) {
                logger.appendMessage(TEST_EMPTY_ENRICHMENTS_COMMANDS);
                attributes.setTestRawResult(specification.getEventContent());
            } else {
                logger.appendMessage(TEST_ENRICHING_COMMANDS_MESSAGE);

                result.getAttributes().getEnrichmentCommands().forEach(x -> logEnrichmentCommands(logger, x));
                ArrayList<Pair<String, String>> enrichments = EnrichmentEvaluatorLibrary
                        .evaluateCommands(result.getAttributes().getEnrichmentCommands(), tables);

                logger.appendMessage(TEST_ENRICHED_MESSAGES);
                enrichments.forEach(x -> logger.appendMessage(x.toString()));

                logger.appendMessage(TEST_ENRICHED_EVENT);
                String enrichedEvent = EnrichmentEvaluatorLibrary
                        .mergeEnrichments(specification.getEventContent(), enrichments, Optional.empty());
                logger.appendMessage(enrichedEvent);
                attributes.setTestRawResult(enrichedEvent);
            }
            attributes.setTestResult(logger.getLog());
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }

        return new EnrichmentResult(OK, attributes);
    }

    private String wrapRuleToRules(String ruleStr) throws IOException {
        RuleDto rule = JSON_RULE_READER.readValue(ruleStr);
        RulesDto rules = new RulesDto();
        rules.setRulesVersion(rule.getRuleVersion());
        rules.setRules(Arrays.asList(rule));
        return JSON_RULES_WRITER.writeValueAsString(rules);
    }

    public static EnrichmentCompiler createEnrichmentsCompiler() throws Exception {
        JsonSchemaValidator rulesSchemaValidator = new SiembolJsonSchemaValidator(RulesDto.class);
        JsonSchemaValidator testSchemaValidator = new SiembolJsonSchemaValidator(TestingSpecificationDto.class);
        return new EnrichmentCompilerImpl(rulesSchemaValidator, testSchemaValidator);
    }
}
