import { ConfigWrapper, ConfigData, Deployment, FileHistory } from ".";
import { TestCaseMap, TestCaseWrapper } from "./test-case";

export interface ConfigStoreState {
    configs: ConfigWrapper<ConfigData>[];
    deployment: Deployment<ConfigWrapper<ConfigData>>;
    deploymentHistory: FileHistory[];
    sortedConfigs: ConfigWrapper<ConfigData>[];
    filteredConfigs: ConfigWrapper<ConfigData>[];
    filteredDeployment: Deployment<ConfigWrapper<ConfigData>>;
    searchTerm: string;
    filterMyConfigs: boolean;
    filterUndeployed: boolean;
    filterUpgradable: boolean;
    releaseSubmitInFlight: boolean;
    editedConfig: ConfigWrapper<ConfigData>;
    editedTestCase: TestCaseWrapper;
    testCaseMap: TestCaseMap;
}
