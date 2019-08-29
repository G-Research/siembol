package uk.co.gresearch.nortem.nikita.compiler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.testing.TestingLogger;
import uk.co.gresearch.nortem.nikita.common.*;
import uk.co.gresearch.nortem.nikita.correlationengine.AlertCounterMetadata;
import uk.co.gresearch.nortem.nikita.correlationengine.CorrelationEngineImpl;
import uk.co.gresearch.nortem.nikita.correlationengine.CorrelationRule;
import uk.co.gresearch.nortem.nikita.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.nikita.common.NikitaResult.StatusCode.OK;

public class NikitaCorrelationRulesCompiler implements NikitaCompiler {
    private static final ObjectReader JSON_RULES_READER =
            new ObjectMapper().readerFor(CorrelationRulesDto.class);
    private static final ObjectWriter JSON_RULES_WRITER =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writerFor(CorrelationRulesDto.class);
    private static final ObjectReader JSON_RULE_READER =
            new ObjectMapper().readerFor(CorrelationRuleDto.class);
    private static final String NOT_IMPLEMENTED_YET_MSG = "Not implememented yet";
    private final JsonSchemaValidator jsonSchemaValidator;

    NikitaCorrelationRulesCompiler(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @Override
    public NikitaResult compile(String rules, TestingLogger logger) {
        NikitaResult validateSchemaResult = validateRulesSyntax(rules);
        if (validateSchemaResult.getStatusCode() != OK) {
            return validateSchemaResult;
        }

        try {
            CorrelationRulesDto rulesDto = JSON_RULES_READER.readValue(rules);
            List<Pair<String, String>> generalConstants = rulesDto.getTags() != null
                    ? rulesDto.getTags()
                    .stream()
                    .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                    .collect(Collectors.toList()) : new ArrayList<>();

            List<Pair<String, Object>> generalProtections = new ArrayList<>();
            if (rulesDto.getRulesProtection() != null) {
                generalProtections.add(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName(),
                        rulesDto.getRulesProtection().getMaxPerHour()));
                generalProtections.add(Pair.of(NikitaFields.MAX_PER_DAY_FIELD.getNikitaCorrelationName(),
                        rulesDto.getRulesProtection().getMaxPerDay()));
            }

            List<CorrelationRule> rulesList = new ArrayList<>();
            for (CorrelationRuleDto ruleDto : rulesDto.getRules()) {
                CorrelationAttributesDto attr = ruleDto.getCorrelationAttributes();
                CorrelationRule.Builder<CorrelationRule> builder = CorrelationRule.builder();
                EnumSet<CorrelationRule.Flags> ruleFlags =
                        attr.getTimeComputationType() == TimeComputationTypeDto.EVENT_TIME
                                ? EnumSet.of(CorrelationRule.Flags.USE_EVENT_TIME)
                                : EnumSet.noneOf(CorrelationRule.Flags.class);

                List<Pair<String, String>> tags = ruleDto.getTags() != null
                        ? ruleDto.getTags()
                        .stream()
                        .map(x -> Pair.of(x.getTagName(), x.getTagValue()))
                        .collect(Collectors.toList()) : new ArrayList<>();

                List<Pair<String, Object>> protections = new ArrayList<>();
                if (ruleDto.getRuleProtection() != null) {
                    protections.add(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName(),
                            ruleDto.getRuleProtection().getMaxPerHour()));
                    protections.add(Pair.of(NikitaFields.MAX_PER_DAY_FIELD.getNikitaCorrelationName(),
                            ruleDto.getRuleProtection().getMaxPerDay()));
                }

                builder
                        .alertsThresholds(attr.getAlertsThreshold())
                        .timeWindowInMs(attr.getTimeUnit().convertToMs(attr.getTimeWindow()))
                        .flags(ruleFlags)
                        .maxLagTimeInSec(attr.getMaxTimeLagInSec())
                        .name(ruleDto.getRuleName())
                        .tags(tags)
                        .protections(protections)
                        .version(ruleDto.getRuleVersion());

                for (CorrelationAlertDto alert : attr.getAlerts()) {
                    EnumSet<AlertCounterMetadata.Flags> flags = alert.getMandatory()
                            ? EnumSet.of(AlertCounterMetadata.Flags.MANDATORY)
                            : EnumSet.noneOf(AlertCounterMetadata.Flags.class);
                    builder.addAlertCounter(alert.getAlert(), alert.getAlertsThreshold(), flags);
                }
                rulesList.add(builder.build());
            }

            NikitaEngine engine = new CorrelationEngineImpl.Builder()
                    .constants(generalConstants)
                    .protections(generalProtections)
                    .correlationRules(rulesList)
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
    public NikitaResult testRule(String rule, String event) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_MSG);
    }

    @Override
    public NikitaResult testRules(String rules, String event) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_MSG);
    }

    public static NikitaCompiler createNikitaCorrelationRulesCompiler() throws Exception {
        JsonSchemaValidator validator = new NortemJsonSchemaValidator(CorrelationRulesDto.class);
        return new NikitaCorrelationRulesCompiler(validator);
    }

    @Override
    public String wrapRuleToRules(String ruleStr) throws IOException {
        CorrelationRuleDto rule = JSON_RULE_READER.readValue(ruleStr);
        CorrelationRulesDto rules = new CorrelationRulesDto();
        rules.setRulesVersion(rule.getRuleVersion());
        rules.setRules(Arrays.asList(rule));
        return JSON_RULES_WRITER.writeValueAsString(rules);
    }

}
