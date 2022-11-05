import { Config, Release, FileHistory } from '.';
import { TestCaseMap, TestCaseWrapper } from './test-case';
import { AdminConfig, ConfigManagerRow, TestSpecificationTesters } from './config-model';
import { FilterConfig } from './ui-metadata-map';

export interface ConfigStoreState {
  configs: Config[];
  release: Release;
  initialRelease: Release;
  releaseHistory: FileHistory[];
  sortedConfigs: Config[];
  searchTerm: string;
  releaseSubmitInFlight: boolean;
  editedConfig: Config;
  editedTestCase: TestCaseWrapper;
  testCaseMap: TestCaseMap;
  adminConfig: AdminConfig;
  pastedConfig: any;
  countChangesInRelease: number;
  configManagerRowData: ConfigManagerRow[];
  serviceFilters: string[];
  isAnyFilterPresent: boolean;
  user: string;
  serviceFilterConfig: FilterConfig;
  testSpecificationTesters: TestSpecificationTesters;
}
