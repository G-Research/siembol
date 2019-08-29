package uk.co.gresearch.nortem.nikita.compiler;

import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.common.testing.InactiveTestingLogger;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.common.testing.TestingLogger;

import java.io.IOException;

import static uk.co.gresearch.nortem.nikita.common.NikitaResult.StatusCode.OK;

public interface NikitaCompiler {

    NikitaResult compile(String rules, TestingLogger logger);

    JsonSchemaValidator getSchemaValidator();

    String wrapRuleToRules(String rule) throws IOException;

    NikitaResult testRules(String rules, String event);

    default NikitaResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    default NikitaResult getSchema() {
        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setRulesSchema(getSchemaValidator().getJsonSchema().getAttributes().getJsonSchema());
        return new NikitaResult(OK, attributes);
    }

    default NikitaResult validateRule(String rule) {
        try {
            String rules = wrapRuleToRules(rule);
            return validateRules(rules);
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
    }

    default NikitaResult validateRules(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
    }

    default NikitaResult testRule(String rule, String event) {
        try {
            String rules = wrapRuleToRules(rule);
            return testRules(rules, event);
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
    }

    default NikitaResult validateRulesSyntax(String rules) {
        try {
            NortemResult validationResult = getSchemaValidator().validate(rules);
            if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
                return NikitaResult.fromErrorMessage(validationResult.getAttributes().getMessage());
            }
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
        return new NikitaResult(OK, null);
    }
}
