package uk.co.gresearch.siembol.alerts.compiler;

import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.CompositeAlertingEngine;
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.common.testing.TestingLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;
/**
 * An object that validates, tests and compiles alerting rules
 *
 * <p>This interface provides functionality for validating, testing and compiling alerting rules.
 * Moreover, it computes and provides json schema for alerting rules.
 *
 *
 * @author  Marian Novotny
 * @see AlertingRulesCompiler
 * @see AlertingCorrelationRulesCompiler
 *
 */
public interface AlertingCompiler {
    /**
     * Compiles rules into alerting engine
     *
     * @param rules json string with alerting rules
     * @param logger logger for debugging
     * @return alerting result with alerting engine
     * @see AlertingResult
     * @see AlertingEngine
     */
    AlertingResult compile(String rules, TestingLogger logger);

    /**
     * Compiles list of rules into alerting engine
     *
     * @param rulesList list of json strings with alerting rules
     * @param logger logger for debugging
     * @return alerting result with alerting engines
     * @see AlertingResult
     * @see AlertingEngine
     */
    default AlertingResult compile(List<String> rulesList, TestingLogger logger) {
        if (rulesList.size() == 1) {
            return compile(rulesList.get(0), logger);
        }

        List<AlertingEngine> engines = new ArrayList<>();
        for (String rules: rulesList) {
            AlertingResult result = compile(rules, logger);
            if (result.getStatusCode() != OK) {
                return result;
            }
            engines.add(result.getAttributes().getEngine());
        }

        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEngine(new CompositeAlertingEngine(engines));
        return new AlertingResult(OK, attributes);
    }

    /**
     * Provides json schema validator for alerting rules
     *
     * @return json schema validator for alerting rules
     * @see JsonSchemaValidator
     *
     */
    JsonSchemaValidator getSchemaValidator();

    /**
     * Wraps an alerting rule into json rules structure
     *
     * @param rule json string with alerting rule
     * @return json string with rules that contain the rule on input
     * @throws IOException if rule is not valid json alerting rule
     *
     */
    String wrapRuleToRules(String rule) throws IOException;

    /**
     * Compiles rules into alerting engine and evaluates an event using the engine
     *
     * @param rules json string with alerting rules
     * @param event json string for evaluation
     * @return alerting result with testing result
     * @see AlertingResult
     */
    AlertingResult testRules(String rules, String event);

    /**
     * Compiles a rule into alerting engine and evaluates an event using the engine
     *
     * @param rule json string with an alerting rule
     * @param event json string for evaluation
     * @return alerting result with testing result
     * @see AlertingResult
     */
    default AlertingResult testRule(String rule, String event) {
        try {
            String rules = wrapRuleToRules(rule);
            return testRules(rules, event);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    /**
     * Compiles rules into alerting engine
     *
     * @param rules json string with alerting rules
     * @return alerting result with alerting engine
     * @see AlertingResult
     * @see AlertingEngine
     */
    default AlertingResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    /**
     * Compiles list of rules into alerting engine
     *
     * @param rulesList list of json strings with alerting rules
     * @return alerting result with alerting engines
     * @see AlertingResult
     * @see AlertingEngine
     */
    default AlertingResult compile(List<String> rulesList) {
        return compile(rulesList, new InactiveTestingLogger());
    }

    /**
     * Provides json schema for alerting rules
     *
     * @return AlertingResult with json schema for alerting rules
     * @see AlertingResult
     *
     */
    default AlertingResult getSchema() {
        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setRulesSchema(getSchemaValidator().getJsonSchema().getAttributes().getJsonSchema());
        return new AlertingResult(OK, attributes);
    }

    /**
     * Validates rule by trying to compile them
     *
     * @param rule json string with an alerting rule
     * @return alerting result with status OK if the rule was able to compile
     * @see AlertingResult
     */
    default AlertingResult validateRule(String rule) {
        try {
            String rules = wrapRuleToRules(rule);
            return validateRules(rules);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    /**
     * Validates rules by trying to compile them
     *
     * @param rules json string with alerting rules
     * @return alerting result with status OK if rules were able to compile
     * @see AlertingResult
     */
    default AlertingResult validateRules(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    /**
     * Validates syntax of rules using json schema validator
     *
     * @param rules json string with alerting rules
     * @return alerting result with status OK if rules have valid syntax
     * @see AlertingResult
     */
    default AlertingResult validateRulesSyntax(String rules) {
        try {
            SiembolResult validationResult = getSchemaValidator().validate(rules);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return AlertingResult.fromErrorMessage(validationResult.getAttributes().getMessage());
            }
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
        return new AlertingResult(OK, null);
    }
}
