import { FileHistory } from './';
import { ConfigTestResult } from './config-model';

export const CONFIG_TAB = { name: 'Edit Config', index: 0 };
export const TESTING_TAB = { name: 'Test Config', index: 1 };
export const TEST_CASE_TAB = { name: 'Test Cases', index: 2 };

export interface TestCaseMap {
    [configName: string]: TestCaseWrapper[];
}

export interface TestCase {
    version: number,
    author: string,
    config_name: string,
    test_case_name: string,
    test_specification: any,
    assertions: Assertion[]
}

export interface TestCaseWrapper {
    testCase: TestCase;
    fileHistory?: FileHistory[];
    testCaseResult?: TestCaseResult;
}

export interface TestCaseResult {
    isRunning?: boolean;
    evaluationResult?: TestCaseEvaluationResult;
    testResult?: ConfigTestResult;
}

export interface TestCaseEvaluationResult {
    number_skipped_assertions: number;
    number_matched_assertions: number;
    number_failed_assertions: number;
    assertion_results?: AssertionResults[];
}

export interface AssertionResults {
    assertion_type: string;
    match: boolean;
    actual_value: string;
    expected_pattern: string;
    negated_pattern: boolean;
}

export interface Assertion {
    assertion_type: string;
    json_path: string;
    expected_pattern: string;
    negated_pattern: string;
    description: string;
    active: boolean;
}

export function isNewTestCase(testCase: TestCaseWrapper): boolean {
    return testCase.testCase.version === 0;
}

export function copyHiddenTestCaseFields(formValue: any, testCase: TestCase): any {
    formValue.author = testCase.author;
    formValue.version = testCase.version;
    formValue.test_case_name = testCase.test_case_name;
    formValue.config_name = testCase.config_name;

    return formValue
}
