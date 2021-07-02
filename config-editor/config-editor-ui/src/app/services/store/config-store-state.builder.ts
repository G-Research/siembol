import { ConfigStoreState } from '@app/model/store-state';
import { cloneDeep } from 'lodash';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { Config, Deployment, FileHistory } from '../../model';
import { TestCaseMap } from '@app/model/test-case';
import { TestCaseWrapper, TestCaseResult } from '../../model/test-case';
import { AdminConfig } from '@app/model/config-model';

export class ConfigStoreStateBuilder {
  private state: ConfigStoreState;

  constructor(oldState: ConfigStoreState) {
    this.state = cloneDeep(oldState);
  }

  configs(configs: Config[]) {
    this.state.configs = configs;
    return this;
  }

  deployment(deployment: Deployment) {
    this.state.deployment = deployment;
    return this;
  }

  adminConfig(config: AdminConfig) {
    this.state.adminConfig = config;
    return this;
  }

  initialDeployment(deployment: Deployment) {
    this.state.initialDeployment = cloneDeep(deployment);
    return this;
  }

  deploymentHistory(deploymentHistory: FileHistory[]) {
    this.state.deploymentHistory = deploymentHistory;
    return this;
  }

  detectOutdatedConfigs(): ConfigStoreStateBuilder {
    this.state.configs.forEach(config => {
      const matchingConfig = this.state.deployment.configs.find(r => !r.isNew && r.name === config.name);
      if (matchingConfig) {
        config.isDeployed = true;
        matchingConfig.isDeployed = true;
        if (matchingConfig.version !== config.version) {
          config.versionFlag = config.version;
          matchingConfig.versionFlag = config.version;
        } else {
          config.versionFlag = -1;
          matchingConfig.versionFlag = -1;
        }
      } else {
        config.isDeployed = false;
        config.versionFlag = -1;
      }
    });
    return this;
  }

  updateTestCasesInConfigs() {
    this.state.configs.forEach(config => {
      config.testCases = this.state.testCaseMap[config.name] || [];
    });
    return this;
  }

  resetEditedTestCase() {
    this.state.editedTestCase = null;
    return this;
  }

  editedTestCase(testCase: TestCaseWrapper) {
    this.state.editedTestCase = testCase;
    return this;
  }

  editedConfigTestCases(testCases: TestCaseWrapper[]) {
    if (this.state.editedConfig) {
      this.state.editedConfig.testCases = testCases;
    }
    return this;
  }

  editedTestCaseResult(testCaseResult: TestCaseResult) {
    if (this.state.editedTestCase) {
      this.state.editedTestCase.testCaseResult = testCaseResult;
    }
    return this;
  }

  reorderConfigsByDeployment(): ConfigStoreStateBuilder {
    let pos = 0;
    this.state.sortedConfigs = cloneDeep(this.state.configs);
    for (const r of this.state.deployment.configs) {
      for (let i = pos; i < this.state.sortedConfigs.length; ++i) {
        if (this.state.sortedConfigs[i].name === r.name) {
          const tmp = this.state.sortedConfigs[pos];
          this.state.sortedConfigs[pos] = this.state.sortedConfigs[i];
          this.state.sortedConfigs[i] = tmp;
        }
      }
      ++pos;
    }

    return this;
  }

  searchTerm(searchTerm: string): ConfigStoreStateBuilder {
    this.state.searchTerm = searchTerm === null || searchTerm === undefined ? '' : searchTerm;
    return this;
  }

  computeFiltered(user: string): ConfigStoreStateBuilder {
    this.state.filteredConfigs = cloneDeep(this.state.sortedConfigs);
    this.state.filteredDeployment = cloneDeep(this.state.deployment);

    if (this.state.filterUndeployed) {
      this.state.filteredDeployment.configs = [];
      this.state.filteredConfigs = this.state.filteredConfigs.filter(r => !r.isDeployed);
    }

    if (this.state.filterUpgradable) {
      this.state.filteredDeployment.configs = this.state.filteredDeployment.configs.filter(d => d.versionFlag > 0);
      this.state.filteredConfigs = this.state.filteredConfigs.filter(r => r.versionFlag > 0);
    }

    if (this.state.filterMyConfigs) {
      this.state.filteredDeployment.configs = this.state.filteredDeployment.configs.filter(r => r.author === user);
      this.state.filteredConfigs = this.state.filteredConfigs.filter(r => r.author === user);
    }

    if (this.state.searchTerm !== undefined && this.state.searchTerm !== '') {
      const lowerCaseSearchTerm = this.state.searchTerm.toLowerCase();
      this.state.filteredDeployment.configs = this.state.filteredDeployment.configs.filter(f =>
        f.name.toLowerCase().includes(lowerCaseSearchTerm) ||
        f.author.toLowerCase().startsWith(lowerCaseSearchTerm) ||
        f.tags === undefined
          ? true
          : f.tags.join(' ').includes(lowerCaseSearchTerm)
      );

      this.state.filteredConfigs = this.state.filteredConfigs.filter(r =>
        r.name.toLowerCase().includes(lowerCaseSearchTerm) ||
        r.author.toLowerCase().startsWith(lowerCaseSearchTerm) ||
        r.tags === undefined
          ? true
          : r.tags.join(' ').includes(lowerCaseSearchTerm)
      );
    }

    return this;
  }

  filterMyConfigs(filterMyConfigs: boolean): ConfigStoreStateBuilder {
    this.state.filterMyConfigs = filterMyConfigs;
    return this;
  }

  filterUndeployed(filterUndeployed: boolean): ConfigStoreStateBuilder {
    this.state.filterUndeployed = filterUndeployed;
    return this;
  }

  filterUpgradable(filterUpgradable: boolean): ConfigStoreStateBuilder {
    this.state.filterUpgradable = filterUpgradable;
    return this;
  }

  addConfigToDeployment(filteredIndex: number) {
    if (filteredIndex < this.state.filteredDeployment.configs.length) {
      return this;
    }

    const configToAdd = cloneDeep(this.state.filteredConfigs[filteredIndex]);
    this.state.deployment.configs.push(configToAdd);
    return this;
  }

  addConfigToDeploymenInPosition(filteredConfigIndex: number, filteredDeploymentIndex: number) {
    if (filteredConfigIndex < this.state.filteredDeployment.configs.length) {
      return this.moveConfigInDeployment(filteredConfigIndex, filteredDeploymentIndex);
    }

    const configToAdd = cloneDeep(this.state.filteredConfigs[filteredConfigIndex]);
    this.state.deployment.configs.push(configToAdd);
    this.state.filteredDeployment.configs.push(configToAdd);
    return this.moveConfigInDeployment(this.state.filteredDeployment.configs.length - 1, filteredDeploymentIndex);
  }

  removeConfigFromDeployment(filteredIndex: number) {
    this.state.deployment.configs = this.state.deployment.configs.filter(
      x => x.name !== this.state.filteredDeployment.configs[filteredIndex].name
    );
    return this;
  }

  moveConfigInDeployment(filteredPreviousIndex: number, filteredCurrentIndex: number) {
    const previousIndex = this.state.deployment.configs.findIndex(
      e => e.name === this.state.filteredDeployment.configs[filteredPreviousIndex].name
    );
    const currentIndex = this.state.deployment.configs.findIndex(
      e => e.name === this.state.filteredDeployment.configs[filteredCurrentIndex].name
    );
    moveItemInArray(this.state.deployment.configs, previousIndex, currentIndex);
    return this;
  }

  upgradeConfigInDeployment(filteredIndex: number) {
    const configName = this.state.filteredDeployment.configs[filteredIndex].name;
    const originalDeploymentIndex = this.state.deployment.configs.findIndex(d => d.name === configName);

    const configToUpgrade = this.state.configs.find(c => c.name === configName);
    this.state.deployment.configs[originalDeploymentIndex] = cloneDeep(configToUpgrade);
    return this;
  }

  releaseSubmitInFlight(releaseSubmitInFlight: boolean) {
    this.state.releaseSubmitInFlight = releaseSubmitInFlight;
    return this;
  }

  editedConfig(editedConfig: Config) {
    this.state.editedConfig = editedConfig;
    return this;
  }

  editedConfigByName(configName: string) {
    this.state.editedConfig = this.state.configs.find(x => x.name === configName);
    return this;
  }

  testCaseMap(testCaseMap: TestCaseMap) {
    this.state.testCaseMap = testCaseMap;
    return this;
  }

  pastedConfig(config: any) {
    this.state.pastedConfig = config;
    return this;
  }

  build(): ConfigStoreState {
    return this.state;
  }
}
