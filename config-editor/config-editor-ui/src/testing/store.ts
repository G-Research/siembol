import { mockAdminConfig } from './adminconfigs';
import { mockDeployment } from './deploymentWrapper';
import { mockTestCaseMap, mockTestCaseWrapper1 } from './testcases';
import { ConfigStoreState } from '@app/model/store-state';
import { mockParserConfig, mockParserConfigCloned } from './configs';
import { cloneDeep } from 'lodash';

export const mockStore: ConfigStoreState = {
  adminConfig: cloneDeep(mockAdminConfig),
  configs: [cloneDeep(mockParserConfig), cloneDeep(mockParserConfigCloned)],
  deployment: cloneDeep(mockDeployment),
  deploymentHistory: [],
  editedConfig: cloneDeep(mockParserConfig),
  editedTestCase: cloneDeep(mockTestCaseWrapper1),
  filterMyConfigs: false,
  filterUndeployed: false,
  filterUpgradable: false,
  filteredConfigs: [cloneDeep(mockParserConfig)],
  filteredDeployment: cloneDeep(mockDeployment),
  initialDeployment: cloneDeep(mockDeployment),
  releaseSubmitInFlight: false,
  searchTerm: '',
  sortedConfigs: [cloneDeep(mockParserConfig)],
  testCaseMap: cloneDeep(mockTestCaseMap),
  pastedConfig: undefined,
};
