import { Config, ConfigData, Deployment, FileHistory } from ".";
import { TestCaseMap, TestCaseWrapper } from "./test-case";

export interface ConfigStoreState {
    configs: Config[];
    deployment: Deployment;
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
}
