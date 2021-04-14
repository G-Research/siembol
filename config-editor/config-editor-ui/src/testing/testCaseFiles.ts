import { mockTestCase1, mockTestCase2, mockTestCase3, mockTestCaseFileHistory } from './testcases';

export const mockTestCaseFile1 =
{
  content: mockTestCase1,
  file_history: [],
  file_name: 'config1-test1.json',
};

export const mockTestCaseFile2 =
{
  content: mockTestCase2,
  file_history: [
    mockTestCaseFileHistory,
  ],
  file_name: 'config1-test2.json',
};

export const mockTestCaseFile3 =
{
  content: mockTestCase3,
  file_history: [],
  file_name: 'config2-test3.json',
};
export const mockTestCaseFiles =
{
  files: [
    mockTestCaseFile1,
    mockTestCaseFile2,
  ],
};
