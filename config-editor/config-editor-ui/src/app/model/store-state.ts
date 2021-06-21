import { Config, ConfigData, Deployment, FileHistory } from '.';
import { TestCase, TestCaseMap, TestCaseWrapper } from './test-case';
import { AdminConfig } from './config-model';

export interface ConfigStoreState {
  configs: Config[];
  deployment: Deployment;
  initialDeployment: Deployment;
  deploymentHistory: FileHistory[];
  sortedConfigs: Config[];
  filteredConfigs: Config[];
  filteredDeployment: Deployment;
  searchTerm: string;
  filterMyConfigs: boolean;
  filterUndeployed: boolean;
  filterUpgradable: boolean;
  releaseSubmitInFlight: boolean;
  editedConfig: Config;
  editedTestCase: TestCaseWrapper;
  testCaseMap: TestCaseMap;
  adminConfig: AdminConfig;
}

export interface ConfigHistory {
  past: Array<ValidConfigState>;
  future: Array<ValidConfigState>;
}

export interface ValidConfigState {
  tabIndex?: number;
  formState: any;
}
