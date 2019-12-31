package uk.co.gresearch.nortem.enrichments.common;

import uk.co.gresearch.nortem.enrichments.evaluation.EnrichmentEvaluator;

import java.util.ArrayList;

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
