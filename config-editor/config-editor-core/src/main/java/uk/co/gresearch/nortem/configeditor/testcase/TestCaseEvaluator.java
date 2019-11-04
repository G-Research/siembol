package uk.co.gresearch.nortem.configeditor.testcase;

import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;

public interface TestCaseEvaluator {

    ConfigEditorResult evaluate(String jsonResult, String testCase);

    ConfigEditorResult validate(String testCase);

    ConfigEditorResult getSchema();
}
