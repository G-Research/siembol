import { mockAdminConfig } from './adminconfigs';
import { mockRelease } from './releaseWrapper';
import { mockTestCaseMap, mockTestCaseWrapper1 } from './testcases';
import { ConfigStoreState } from '@app/model/store-state';
import { mockParserConfig, mockParserConfigCloned } from './configs';
import { cloneDeep } from 'lodash';

export const mockStore: ConfigStoreState = {
  adminConfig: cloneDeep(mockAdminConfig),
  configs: [cloneDeep(mockParserConfig), cloneDeep(mockParserConfigCloned)],
  release: cloneDeep(mockRelease),
  releaseHistory: [],
  editedConfig: cloneDeep(mockParserConfig),
  editedTestCase: cloneDeep(mockTestCaseWrapper1),
  initialRelease: cloneDeep(mockRelease),
  releaseSubmitInFlight: false,
  searchTerm: '',
  sortedConfigs: [cloneDeep(mockParserConfig), cloneDeep(mockParserConfigCloned)],
  testCaseMap: cloneDeep(mockTestCaseMap),
  pastedConfig: undefined,
  configManagerRowData: [],
  countChangesInRelease: 0,
  serviceFilters: {},
  isAnyFilterPresent: false,
  serviceFilterConfig: {},
  user: "siembol",
};
