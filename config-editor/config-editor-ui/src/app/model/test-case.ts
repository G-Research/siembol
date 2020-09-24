import { FileHistory } from './';
import { ConfigTestResult } from './config-model';

export const TEST_CASE_TAB_NAME = 'Test Cases';
export const TESTING_TAB_NAME = 'Test Config';

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
