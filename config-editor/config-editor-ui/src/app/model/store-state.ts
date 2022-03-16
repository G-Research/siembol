import { Config, Release, FileHistory } from '.';
import { TestCaseMap, TestCaseWrapper } from './test-case';
import { AdminConfig, ConfigManagerRow, EnabledCheckboxFilters } from './config-model';

export interface ConfigStoreState {
  configs: Config[];
  release: Release;
  initialRelease: Release;
  releaseHistory: FileHistory[];
  sortedConfigs: Config[];
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
  enabledCheckboxFilters: EnabledCheckboxFilters;
}
