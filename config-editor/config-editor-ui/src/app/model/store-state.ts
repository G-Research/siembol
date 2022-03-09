import { Config, Release, FileHistory } from '.';
import { TestCaseMap, TestCaseWrapper } from './test-case';
import { AdminConfig } from './config-model';

export interface ConfigStoreState {
  configs: Config[];
  release: Release;
  initialRelease: Release;
  releaseHistory: FileHistory[];
  sortedConfigs: Config[];
  filteredConfigs: Config[];
  filteredRelease: Release;
  searchTerm: string;
  filterMyConfigs: boolean;
  filterUndeployed: boolean;
  filterUpgradable: boolean;
  releaseSubmitInFlight: boolean;
  editedConfig: Config;
  editedTestCase: TestCaseWrapper;
  testCaseMap: TestCaseMap;
  adminConfig: AdminConfig;
  pastedConfig: any;
}
