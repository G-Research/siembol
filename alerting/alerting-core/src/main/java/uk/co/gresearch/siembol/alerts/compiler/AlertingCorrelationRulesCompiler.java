package uk.co.gresearch.siembol.alerts.compiler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.alerts.common.*;
import uk.co.gresearch.siembol.alerts.correlationengine.AlertCounterMetadata;
import uk.co.gresearch.siembol.alerts.correlationengine.CorrelationEngineImpl;
import uk.co.gresearch.siembol.alerts.correlationengine.CorrelationRule;
import uk.co.gresearch.siembol.alerts.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;
/**
 * An object that validates and compiles correlation alerting rules
 *
 * <p>This objects provides functionality for validating and compiling correlation alerting rules.
 * Moreover, it computes and provides json schema for correlation rules.
 *
 *
 * @author  Marian Novotny
 * @see AlertingCompiler
 *
 */

public class AlertingCorrelationRulesCompiler implements AlertingCompiler {
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

    AlertingCorrelationRulesCompiler(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertingResult compile(String rules, TestingLogger logger) {
        AlertingResult validateSchemaResult = validateRulesSyntax(rules);
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
                generalProtections.add(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName(),
                        rulesDto.getRulesProtection().getMaxPerHour()));
                generalProtections.add(Pair.of(AlertingFields.MAX_PER_DAY_FIELD.getCorrelationAlertingName(),
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
                    protections.add(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName(),
                            ruleDto.getRuleProtection().getMaxPerHour()));
                    protections.add(Pair.of(AlertingFields.MAX_PER_DAY_FIELD.getCorrelationAlertingName(),
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

                if (attr.getFieldsToSend() != null) {
                    builder.fieldNamesToSend(attr.getFieldsToSend());
                }

                for (CorrelationAlertDto alert : attr.getAlerts()) {
                    EnumSet<AlertCounterMetadata.Flags> flags = alert.getMandatory()
                            ? EnumSet.of(AlertCounterMetadata.Flags.MANDATORY)
                            : EnumSet.noneOf(AlertCounterMetadata.Flags.class);
                    builder.addAlertCounter(alert.getAlert(), alert.getAlertsThreshold(), flags);
                }
                rulesList.add(builder.build());
            }

            AlertingEngine engine = new CorrelationEngineImpl.Builder()
                    .constants(generalConstants)
                    .protections(generalProtections)
                    .correlationRules(rulesList)
                    .build();

            AlertingAttributes attributes = new AlertingAttributes();
            attributes.setEngine(engine);
            return new AlertingResult(OK, attributes);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaValidator getSchemaValidator() {
        return jsonSchemaValidator;
    }

    /**
     * This method is not implemented yet
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public AlertingResult testRule(String rule, String event) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_MSG);
    }

    /**
     * This method is not implemented yet
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public AlertingResult testRules(String rules, String event) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_MSG);
    }

    /**
     * Factory method for creating AlertingCorrelationRulesCompiler instance
     *
     * @return AlertingCorrelationRulesCompiler instance
     * @throws Exception if creation of the instance fails
     */
    public static AlertingCompiler createAlertingCorrelationRulesCompiler() throws Exception {
        JsonSchemaValidator validator = new SiembolJsonSchemaValidator(CorrelationRulesDto.class);
        return new AlertingCorrelationRulesCompiler(validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String wrapRuleToRules(String ruleStr) throws IOException {
        CorrelationRuleDto rule = JSON_RULE_READER.readValue(ruleStr);
        CorrelationRulesDto rules = new CorrelationRulesDto();
        rules.setRulesVersion(rule.getRuleVersion());
        rules.setRules(Arrays.asList(rule));
        return JSON_RULES_WRITER.writeValueAsString(rules);
    }
}
