import { Config, Release, FileHistory } from '.';
import { TestCaseMap, TestCaseWrapper } from './test-case';
import { AdminConfig, ConfigManagerRow } from './config-model';

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
  filterUnreleased: boolean;
  filterUpgradable: boolean;
  releaseSubmitInFlight: boolean;
  editedConfig: Config;
  editedTestCase: TestCaseWrapper;
  testCaseMap: TestCaseMap;
  adminConfig: AdminConfig;
  pastedConfig: any;
  countChangesInRelease: number;
  configRowData: ConfigManagerRow[];
}
