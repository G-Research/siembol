import { TestCaseWrapper, TestCase } from "@app/model/test-case";

export const mockTestCaseFileHistory =
{
  added: 22,
  author: 'siembol',
  date: '2021-02-22T10:28:19',
  removed: 0,
};

export const mockTestCase1: TestCase =
{
  assertions: [
    {
      active: true,
      assertion_type: 'path_and_value_matches',
      description: null,
      expected_pattern: 'value',
      json_path: '$.parsedMessages[0].key',
      negated_pattern: false,
    },
  ],
  author: 'siembol',
  config_name: 'config1',
  description: null,
  test_case_name: 'test1',
  test_specification: {
    encoding: 'utf8_string',
    log: {
      key: 'value',
      source_type: 'test',
    },
  },
  version: 7,
};

export const mockTestCase2 =
{
  assertions: [
    {
      active: true,
      assertion_type: 'only_if_path_exists',
      description: null,
      expected_pattern: 'value2',
      json_path: '$.parsedMessages[0].key',
      negated_pattern: false,
    },
  ],
  author: 'siembol',
  config_name: 'config1',
  description: 'testing',
  test_case_name: 'test2',
  test_specification: {
    encoding: 'utf8_string',
    log: {
      key: 'value2',
      source_type: 'source',
    },
  },
  version: 1,
};

export const mockTestCase3 =
{
  assertions: [
    {
      active: true,
      assertion_type: 'only_if_path_exists',
      description: null,
      expected_pattern: 'value3',
      json_path: '$.key',
      negated_pattern: false,
    },
  ],
  author: 'siembol',
  config_name: 'config2',
  description: 'testing',
  test_case_name: 'test3',
  test_specification: {
    event: {
      key: 'value3',
      source_type: 'source',
    },
  },
  version: 1,
};

export const mockTestCaseWrapper1: TestCaseWrapper =
{
  fileHistory: [],
  testCase: mockTestCase1,
  testCaseResult: null,
};

export const mockTestCaseWrapper2 =
{
  fileHistory: [
    mockTestCaseFileHistory,
  ],
  testCase: mockTestCase2,
  testCaseResult: null,
};

export const mockTestCaseWrapper3 =
{
  fileHistory: [
    mockTestCaseFileHistory,
  ],
  testCase: mockTestCase3,
  testCaseResult: null,
};

export const mockTestCaseMap =
{
  config1: [
    mockTestCaseWrapper1,
    mockTestCaseWrapper2,
  ],
};


