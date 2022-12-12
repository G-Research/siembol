package uk.co.gresearch.siembol.enrichments.common;

import uk.co.gresearch.siembol.enrichments.evaluation.EnrichmentEvaluator;

import java.util.ArrayList;
/**
 * An object for representing enrichment attributes
 *
 * <p>This class represents enrichment attributes such as an enrichment evaluator and enrichment commands that
 * are used in the enrichment result.
 *
 * @author  Marian Novotny
 * @see EnrichmentEvaluator
 * @see EnrichmentCommand
 */
public class EnrichmentAttributes {
    private String rulesSchema;
    private String testSchema;
    private String message;
    private String testResult;
    private String testRawResult;
    private EnrichmentEvaluator ruleEvaluator;
    private ArrayList<EnrichmentCommand> enrichmentCommands;

    public String getRulesSchema() {
        return rulesSchema;
    }

    public void setRulesSchema(String rulesSchema) {
        this.rulesSchema = rulesSchema;
    }

    public String getTestSchema() {
        return testSchema;
    }

    public void setTestSchema(String testSchema) {
        this.testSchema = testSchema;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getTestRawResult() {
        return testRawResult;
    }

    public void setTestRawResult(String testRawResult) {
        this.testRawResult = testRawResult;
    }

    public EnrichmentEvaluator getRuleEvaluator() {
        return ruleEvaluator;
    }

    public void setRuleEvaluator(EnrichmentEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    public ArrayList<EnrichmentCommand> getEnrichmentCommands() {
        return enrichmentCommands;
    }

    public void setEnrichmentCommands(ArrayList<EnrichmentCommand> enrichmentCommands) {
        this.enrichmentCommands = enrichmentCommands;
    }
}
