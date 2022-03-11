import { ConfigStoreState } from '@app/model/store-state';
import { cloneDeep } from 'lodash';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { Config, Release, FileHistory } from '../../model';
import { TestCaseMap } from '@app/model/test-case';
import { TestCaseWrapper, TestCaseResult } from '../../model/test-case';
import { AdminConfig, ConfigManagerRow } from '@app/model/config-model';

export class ConfigStoreStateBuilder {
  private state: ConfigStoreState;

  constructor(oldState: ConfigStoreState) {
    this.state = cloneDeep(oldState);
  }

  configs(configs: Config[]) {
    this.state.configs = configs;
    return this;
  }

  release(release: Release) {
    this.state.release = release;
    return this;
  }

  adminConfig(config: AdminConfig) {
    this.state.adminConfig = config;
    return this;
  }

  initialRelease(release: Release) {
    this.state.initialRelease = cloneDeep(release);
    return this;
  }

  releaseHistory(releaseHistory: FileHistory[]) {
    this.state.releaseHistory = releaseHistory;
    return this;
  }

  detectOutdatedConfigs(): ConfigStoreStateBuilder {
    this.state.configs.forEach(config => {
      const matchingConfig = this.state.release.configs.find(r => !r.isNew && r.name === config.name);
      if (matchingConfig) {
        config.isReleased = true;
        matchingConfig.isReleased = true;
        if (matchingConfig.version !== config.version) {
          config.versionFlag = config.version;
          matchingConfig.versionFlag = config.version;
        } else {
          config.versionFlag = -1;
          matchingConfig.versionFlag = -1;
        }
      } else {
        config.isReleased = false;
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

  reorderConfigsByRelease(): ConfigStoreStateBuilder {
    let pos = 0;
    this.state.sortedConfigs = cloneDeep(this.state.configs);
    for (const r of this.state.release.configs) {
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
    this.state.filteredRelease = cloneDeep(this.state.release);

    // if (this.state.filterUnreleased) {
    //   this.state.filteredRelease.configs = [];
    //   this.state.filteredConfigs = this.state.filteredConfigs.filter(r => !r.isReleased);
    // }

    // if (this.state.filterUpgradable) {
    //   this.state.filteredRelease.configs = this.state.filteredRelease.configs.filter(d => d.versionFlag > 0);
    //   this.state.filteredConfigs = this.state.filteredConfigs.filter(r => r.versionFlag > 0);
    // }

    // if (this.state.filterMyConfigs) {
    //   this.state.filteredRelease.configs = this.state.filteredRelease.configs.filter(r => r.author === user);
    //   this.state.filteredConfigs = this.state.filteredConfigs.filter(r => r.author === user);
    // }

    // if (this.state.searchTerm !== undefined && this.state.searchTerm !== '') {
    //   this.state.filteredRelease.configs = this.state.filteredRelease.configs.filter(c => this.filterSearchTerm(c));
    //   this.state.filteredConfigs = this.state.filteredConfigs.filter(c => this.filterSearchTerm(c));
    // }

    return this;
  }

  filterMyConfigs(filterMyConfigs: boolean): ConfigStoreStateBuilder {
    this.state.filterMyConfigs = filterMyConfigs;
    return this;
  }

  filterUnreleased(filterUnreleased: boolean): ConfigStoreStateBuilder {
    this.state.filterUnreleased = filterUnreleased;
    return this;
  }

  filterUpgradable(filterUpgradable: boolean): ConfigStoreStateBuilder {
    this.state.filterUpgradable = filterUpgradable;
    return this;
  }

  addConfigToRelease(filteredIndex: number) {
    if (filteredIndex < this.state.filteredRelease.configs.length) {
      return this;
    }

    const configToAdd = cloneDeep(this.state.filteredConfigs[filteredIndex]);
    this.state.release.configs.push(configToAdd);
    return this;
  }

  removeConfigFromRelease(filteredIndex: number) {
    this.state.release.configs = this.state.release.configs.filter(
      x => x.name !== this.state.filteredRelease.configs[filteredIndex].name
    );
    return this;
  }

  moveConfigInRelease(configName: string, filteredCurrentIndex: number) {
    const previousIndex = this.state.release.configs.findIndex(
      e => e.name === configName
    );
    const currentIndex = this.state.release.configs.findIndex(
      e => e.name === this.state.filteredRelease.configs[filteredCurrentIndex]?.name
    );
    if (currentIndex === -1) {
      return this;
    }
    moveItemInArray(this.state.release.configs, previousIndex, currentIndex);
    return this;
  }

  upgradeConfigInRelease(filteredIndex: number) {
    const configName = this.state.filteredRelease.configs[filteredIndex].name;
    const originalReleaseIndex = this.state.release.configs.findIndex(d => d.name === configName);

    const configToUpgrade = this.state.configs.find(c => c.name === configName);
    this.state.release.configs[originalReleaseIndex] = cloneDeep(configToUpgrade);
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

  incrementChangesInRelease() {
    this.state.countChangesInRelease += 1;
    return this;
  }

  resetChangesInRelease() {
    this.state.countChangesInRelease = 0;
    return this;
  }

  computeRowData() {
    this.state.configRowData = this.state.filteredConfigs.map(
      (config: Config) => this.getRowFromConfig(config, this.state.filteredRelease)
    );
    return this;
  }

  private getRowFromConfig(config: Config, release: Release): ConfigManagerRow {
    const releaseConfig = release.configs.find(x => x.name === config.name);
    const releaseVersion = releaseConfig? releaseConfig.version : 0;
    return ({
      author: config.author, 
      version: config.version, 
      config_name: config.name, 
      releasedVersion:  releaseVersion,
      configHistory: config.fileHistory,
      labels_: config.tags,
      testCasesCount: config.testCases.length,
    });
  }

  // private filterSearchTerm(config: any) {
  //   const lowerCaseSearchTerm = this.state.searchTerm.toLowerCase();
  //   return config.name.toLowerCase().includes(lowerCaseSearchTerm) ||
  //          config.author.toLowerCase().startsWith(lowerCaseSearchTerm) ||
  //          config.tags === undefined ? true : config.tags.join(' ').toLowerCase().includes(lowerCaseSearchTerm)
  // }
}
