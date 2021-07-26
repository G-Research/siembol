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

public interface AlertingCompiler {
    AlertingResult compile(String rules, TestingLogger logger);

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

    JsonSchemaValidator getSchemaValidator();

    String wrapRuleToRules(String rule) throws IOException;

    AlertingResult testRules(String rules, String event);

    default AlertingResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    default AlertingResult compile(List<String> rulesList) {
        return compile(rulesList, new InactiveTestingLogger());
    }

    default AlertingResult getSchema() {
        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setRulesSchema(getSchemaValidator().getJsonSchema().getAttributes().getJsonSchema());
        return new AlertingResult(OK, attributes);
    }

    default AlertingResult validateRule(String rule) {
        try {
            String rules = wrapRuleToRules(rule);
            return validateRules(rules);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    default AlertingResult validateRules(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    default AlertingResult testRule(String rule, String event) {
        try {
            String rules = wrapRuleToRules(rule);
            return testRules(rules, event);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

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
