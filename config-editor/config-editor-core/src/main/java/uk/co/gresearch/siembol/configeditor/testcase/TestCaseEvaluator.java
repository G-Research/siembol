package uk.co.gresearch.siembol.configeditor.testcase;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

public interface TestCaseEvaluator {

    ConfigEditorResult evaluate(String jsonResult, String testCase);

    ConfigEditorResult validate(String testCase);

    ConfigEditorResult getSchema();
}
