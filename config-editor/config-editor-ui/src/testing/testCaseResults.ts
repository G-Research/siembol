import { TestCaseEvaluationResult, AssertionResults, TestCaseResult } from '@app/model/test-case';
import { ConfigTestResult } from '@app/model/config-model';
import { mockTestCase1 } from './testcases';


export const mockConfigTestResultNoMatch =
{
   test_result_complete: true,
   test_result_output: '',
   test_result_raw_output: {},
};

export const mockConfigTestResultMatch: ConfigTestResult =
{
   test_result_complete: true,
   test_result_output: '[ {\n  "original_string" : "{\\"key\\":\\"value\\"}",\n  "source_type" : "test",\n  "key" : "value",\n  "timestamp" : 1615808366615\n} ]',
   test_result_raw_output: {
      parsedMessages: [
         {
            key: 'value',
            original_string: '{"key":"value"}',
            source_type: 'test',
            timestamp: 1615808366615,
         },
      ],
      sourceType: 'test',
   },
};

export const mockEvaluationResultNoMatch: TestCaseEvaluationResult =
{
   assertion_results: [
      {
         actual_value: null,
         assertion_type: 'path_and_value_matches',
         expected_pattern: 'test',
         match: false,
         negated_pattern: false,
      },
   ],
   number_failed_assertions: 1,
   number_matched_assertions: 0,
   number_skipped_assertions: 0,
};

export const mockEvaluationResultMatch =
{
   assertion_results: [
      {
         actual_value: 'value',
         assertion_type: 'path_and_value_matches',
         expected_pattern: 'value',
         match: true,
         negated_pattern: false,
      },
   ],
   number_failed_assertions: 0,
   number_matched_assertions: 1,
   number_skipped_assertions: 0,
};

export const mockEvaluateTestCaseNoMatch =
{
   evaluationResult: mockEvaluationResultNoMatch,
   isRunning: false,
   testResult: mockConfigTestResultNoMatch,
};

export const mockEvaluateTestCaseMatch: TestCaseResult =
{
   evaluationResult: mockEvaluationResultMatch,
   isRunning: false,
   testResult: mockConfigTestResultMatch,
};

export const mockEvaluateTestCaseFromResult =
{
   files: [{
      content: mockTestCase1,
   }],
   test_result_raw_output: '{}',
};
