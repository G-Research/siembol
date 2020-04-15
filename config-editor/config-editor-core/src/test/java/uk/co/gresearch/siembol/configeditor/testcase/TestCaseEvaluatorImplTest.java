package uk.co.gresearch.siembol.configeditor.testcase;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorTestCaseResult;


import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class TestCaseEvaluatorImplTest {
    /**
     * {
     *    "a" : "tmp",
     *    "b" : true,
     *    "c" : "test",
     *    "d" : { "e" : "test"},
     *    "f" : [ "1", "2"]
     * }
     **/
    @Multiline
    public static String testResult;
    /**
     * {
     *   "test_case_name": "test",
     *   "version": 1,
     *   "author": "john",
     *   "config_name": "syslog",
     *   "description": "unitest test case",
     *   "test_specification": {
     *     "secret": true
     *   },
     *   "assertions": [
     *     {
     *       "assertion_type": "path_and_value_matches",
     *       "json_path": "$.a",
     *       "expected_pattern": "^.*mp$",
     *       "negated_pattern": false,
     *       "description": "match string",
     *       "active": true
     *     },
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "s",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "skipped assertion",
     *       "active": false
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String simpleTestCaseString;

    /**
     * {
     *   "test_case_name": "test",
     *   "version": 1,
     *   "author": "john",
     *   "config_name": "syslog",
     *   "description": "unitest test case",
     *   "test_specification": {
     *     "secret": true
     *   },
     *   "assertions": [
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "$.b",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "negated match of boolean",
     *       "active": true
     *     },
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "s",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "skipped assertion",
     *       "active": false
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String simpleTestCaseBoolean;

    /**
     * {
     *   "test_case_name": "test",
     *   "version": 1,
     *   "author": "john",
     *   "config_name": "syslog",
     *   "description": "unitest test case",
     *   "test_specification": {
     *     "secret": true
     *   },
     *   "assertions": [
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "$.d",
     *       "expected_pattern": ".*ask",
     *       "negated_pattern": false,
     *       "description": "fail to match object",
     *       "active": true
     *     },
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "s",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "skipped assertion",
     *       "active": false
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String simpleTestCaseObject;

    /**
     * {
     *   "test_case_name": "test",
     *   "version": 1,
     *   "author": "john",
     *   "config_name": "syslog",
     *   "description": "unitest test case",
     *   "test_specification": {
     *     "secret": true
     *   },
     *   "assertions": [
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "$.f",
     *       "expected_pattern": ".*1.*",
     *       "negated_pattern": false,
     *       "description": "match in array",
     *       "active": true
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String simpleTestCaseArray;

    /**
     * {
     *   "test_case_name": "test",
     *   "version": 1,
     *   "author": "john",
     *   "config_name": "syslog",
     *   "description": "unitest test case",
     *   "test_specification": {
     *     "secret": true
     *   },
     *   "assertions": [
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "$.g",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "only if path exists test",
     *       "active": true
     *     },
     *     {
     *       "assertion_type": "only_if_path_exists",
     *       "json_path": "s",
     *       "expected_pattern": "secret",
     *       "negated_pattern": true,
     *       "description": "skipped assertion",
     *       "active": false
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String simpleTestCaseMissing;

    private TestCaseEvaluator testCaseEvaluator;

    @Before
    public void setUp() throws Exception {
        testCaseEvaluator = new TestCaseEvaluatorImpl();
    }

    @Test
    public void getSchema() {
        ConfigEditorResult result = testCaseEvaluator.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test
    public void evaluateString() {
        ConfigEditorResult result = testCaseEvaluator.evaluate(testResult, simpleTestCaseString);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestCaseResult());
        ConfigEditorTestCaseResult caseResult = result.getAttributes().getTestCaseResult();
        Assert.assertEquals(Integer.valueOf(0), caseResult.getFailedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getMatchedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getSkippedAssertions());

        Assert.assertEquals(1, caseResult.getAssertionResults().size());
        Assert.assertEquals("path_and_value_matches",
                caseResult.getAssertionResults().get(0).getAssertionType());
        Assert.assertEquals("tmp",
                caseResult.getAssertionResults().get(0).getActualValue());
        Assert.assertEquals("^.*mp$",
                caseResult.getAssertionResults().get(0).getExpectedPattern());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getMatch());
        Assert.assertFalse(caseResult.getAssertionResults().get(0).getNegated());
    }

    @Test
    public void evaluateBoolean() {
        ConfigEditorResult result = testCaseEvaluator.evaluate(testResult, simpleTestCaseBoolean);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestCaseResult());
        ConfigEditorTestCaseResult caseResult = result.getAttributes().getTestCaseResult();
        Assert.assertEquals(Integer.valueOf(0), caseResult.getFailedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getMatchedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getSkippedAssertions());

        Assert.assertEquals(1, caseResult.getAssertionResults().size());
        Assert.assertEquals("only_if_path_exists",
                caseResult.getAssertionResults().get(0).getAssertionType());
        Assert.assertEquals("true",
                caseResult.getAssertionResults().get(0).getActualValue());
        Assert.assertEquals("secret",
                caseResult.getAssertionResults().get(0).getExpectedPattern());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getMatch());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getNegated());
    }

    @Test
    public void evaluateObject() {
        ConfigEditorResult result = testCaseEvaluator.evaluate(testResult, simpleTestCaseObject);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestCaseResult());
        ConfigEditorTestCaseResult caseResult = result.getAttributes().getTestCaseResult();
        Assert.assertEquals(Integer.valueOf(1), caseResult.getFailedAssertions());
        Assert.assertEquals(Integer.valueOf(0), caseResult.getMatchedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getSkippedAssertions());

        Assert.assertEquals(1, caseResult.getAssertionResults().size());
        Assert.assertEquals("only_if_path_exists",
                caseResult.getAssertionResults().get(0).getAssertionType());
        Assert.assertEquals("{\"e\":\"test\"}",
                caseResult.getAssertionResults().get(0).getActualValue());
        Assert.assertEquals(".*ask",
                caseResult.getAssertionResults().get(0).getExpectedPattern());
        Assert.assertFalse(caseResult.getAssertionResults().get(0).getMatch());
        Assert.assertFalse(caseResult.getAssertionResults().get(0).getNegated());
    }

    @Test
    public void evaluateArray() {
        ConfigEditorResult result = testCaseEvaluator.evaluate(testResult, simpleTestCaseArray);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestCaseResult());
        ConfigEditorTestCaseResult caseResult = result.getAttributes().getTestCaseResult();
        Assert.assertEquals(Integer.valueOf(0), caseResult.getFailedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getMatchedAssertions());
        Assert.assertEquals(Integer.valueOf(0), caseResult.getSkippedAssertions());

        Assert.assertEquals(1, caseResult.getAssertionResults().size());
        Assert.assertEquals("only_if_path_exists",
                caseResult.getAssertionResults().get(0).getAssertionType());
        Assert.assertEquals("[\"1\",\"2\"]",
                caseResult.getAssertionResults().get(0).getActualValue());
        Assert.assertEquals(".*1.*",
                caseResult.getAssertionResults().get(0).getExpectedPattern());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getMatch());
        Assert.assertFalse(caseResult.getAssertionResults().get(0).getNegated());
    }

    @Test
    public void evaluateMissing() {
        ConfigEditorResult result = testCaseEvaluator.evaluate(testResult, simpleTestCaseMissing);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestCaseResult());
        ConfigEditorTestCaseResult caseResult = result.getAttributes().getTestCaseResult();
        Assert.assertEquals(Integer.valueOf(0), caseResult.getFailedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getMatchedAssertions());
        Assert.assertEquals(Integer.valueOf(1), caseResult.getSkippedAssertions());

        Assert.assertEquals(1, caseResult.getAssertionResults().size());
        Assert.assertEquals("only_if_path_exists",
                caseResult.getAssertionResults().get(0).getAssertionType());
        Assert.assertNull(caseResult.getAssertionResults().get(0).getActualValue());
        Assert.assertEquals("secret",
                caseResult.getAssertionResults().get(0).getExpectedPattern());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getMatch());
        Assert.assertTrue(caseResult.getAssertionResults().get(0).getNegated());
    }

    @Test
    public void validateOK() {
        ConfigEditorResult result = testCaseEvaluator.validate(simpleTestCaseString);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateInvalidJson() {
        ConfigEditorResult result = testCaseEvaluator.validate("INVALID");
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("Unrecognized token 'INVALID'"));
    }

    @Test
    public void validateMissingRequiredField() {
        ConfigEditorResult result = testCaseEvaluator.validate(simpleTestCaseString.replace("test_case_name",
                "unsupported_name"));
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage()
                .contains("missing required properties ([\"test_case_name\"]"));
    }

    @Test
    public void validateInvalidJsonPath() {
        ConfigEditorResult result = testCaseEvaluator.validate(simpleTestCaseString.replace("$.a",
                "$$"));
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage()
                .contains("InvalidPathException"));
    }

    @Test
    public void validateInvalidRegexPattern() {
        ConfigEditorResult result = testCaseEvaluator.validate(simpleTestCaseString.replace("^.*mp$",
                "["));
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage()
                .contains("PatternSyntaxException"));
    }
}
