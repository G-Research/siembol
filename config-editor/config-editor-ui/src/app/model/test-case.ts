import { FileHistory } from './';

export const TEST_CASE_TAB_NAME = 'Test Cases';
export const TESTING_TAB_NAME = 'Test Config';

export interface TestCaseMap {
    [configName: string]: TestCaseWrapper[];
}

export type TestCase = any;

export interface TestCaseWrapper {
    testCase: TestCase;
    testState: TestState;
    testResult: TestCaseResult;
    fileHistory?: FileHistory[];
}

export interface TestCaseResult {
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

export enum TestState {
    PASS,
    FAIL,
    NOT_RUN,
    RUNNING,
    ERROR,
}

export interface Assertion {
    assertion_type: string;
    json_path: string;
    expected_pattern: string;
    negated_pattern: string;
    description: string;
    active: boolean;
}

export class TestCaseResultDefault implements TestCaseResult {
    number_skipped_assertions = 0;
    number_matched_assertions = 0;
    number_failed_assertions = 0;
    assertion_results: AssertionResults[] = [];

    constructor() {}
}

export class TestCaseWrapperDefault implements TestCaseWrapper {
    testCase: TestCase = {};
    testState: TestState = TestState.NOT_RUN;
    testResult: TestCaseResult = new TestCaseResultDefault();

    constructor() {}
}
