import { ConfigStoreState } from '@app/model/store-state';
import { cloneDeep } from 'lodash';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { Config, Release, FileHistory } from '../../model';
import { TestCaseMap } from '@app/model/test-case';
import { TestCaseWrapper, TestCaseResult } from '../../model/test-case';
import { AdminConfig, ConfigManagerRow, ConfigStatus, FILTER_DELIMITER } from '@app/model/config-model';
import { FilterConfig, UiMetadata } from '@app/model/ui-metadata-map';
import { ParamMap } from '@angular/router';

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

  updateServiceFilters(params: ParamMap): ConfigStoreStateBuilder {
    this.state.serviceFilters = [];
    params.keys.forEach(key => {
      if (Object.keys(this.state.serviceFilterConfig).includes(key)) {
        params.getAll(key).forEach(filter => {
          const filterName = key + FILTER_DELIMITER + filter;
          this.state.serviceFilters.push(filterName);
        })
      }
    })
    return this;
  }

  addConfigToRelease(name: string) {
    const configToAdd = cloneDeep(this.state.configs.find(c => c.name === name));
    this.state.release.configs.push(configToAdd);
    return this;
  }

  removeConfigFromRelease(name: string) {
    this.state.release.configs = this.state.release.configs.filter(
      x => x.name !== name
    );
    return this;
  }

  moveConfigInRelease(configName: string, filteredCurrentIndex: number) {
    const previousIndex = this.state.release.configs.findIndex(
      e => e.name === configName
    );
    const currentIndex = this.state.release.configs.findIndex(
      e => e.name === this.state.release.configs[filteredCurrentIndex]?.name
    );
    if (currentIndex === -1) {
      return this;
    }
    moveItemInArray(this.state.release.configs, previousIndex, currentIndex);
    return this;
  }

  upgradeConfigInRelease(configName: string) {
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

  user(user: string) {
    this.state.user = user;
    return this;
  }

  serviceFilterConfig(uiMetadata: UiMetadata) {
    this.state.serviceFilterConfig = { 
      ...uiMetadata.checkboxes,
      ...this.getCommonFilters(),
    };
    return this;
  }

  computeConfigManagerRowData() {
    this.state.isAnyFilterPresent = this.state.serviceFilters.length > 0;
    this.state.configManagerRowData = this.state.sortedConfigs.map(
      (config: Config) => this.getRowFromConfig(config, this.state.release)
    );
    return this;
  }

  private evaluateFilters(node: ConfigManagerRow): boolean {
    for (const name of this.state.serviceFilters) {
      if (!this.evaluateSingleFilter(name, node)) {
        return false;
      }
    }
    return true;
  }

  private evaluateSingleFilter(
    name: string, 
    node: ConfigManagerRow
  ): boolean {
    const [groupName, filterName] = name.split(FILTER_DELIMITER, 2);
    const filter = this.state.serviceFilterConfig[groupName][filterName];
    const re = new RegExp(filter.pattern, 'i');
    const value = node[filter.field];
    if (value instanceof Array) {
      return value.some(v => re.test(v));
    }
    return re.test(value);
  }

  private getRowFromConfig(config: Config, release: Release): ConfigManagerRow {
    const releaseConfig = release.configs.find(x => x.name === config.name);
    const releaseVersion = releaseConfig? releaseConfig.version : 0;
    const row = {
      author: config.author, 
      version: config.version, 
      config_name: config.name, 
      releasedVersion:  releaseVersion,
      configHistory: config.fileHistory,
      labels: config.tags,
      testCasesCount: config.testCases.length,
      status: this.getStatus(config.version, releaseVersion),
      isFiltered: true,
    };
    if (this.state.isAnyFilterPresent) {
      row.isFiltered = this.evaluateFilters(row);;
    }
    return row;
  }

  private getStatus(lastVersion: number, releasedVersion: number) {
    switch(releasedVersion) {
      case(0):
        return ConfigStatus.UNRELEASED;
      case(lastVersion):
        return ConfigStatus.UP_TO_DATE;
      default:
        return ConfigStatus.UPGRADABLE;
    }
  }

  private getCommonFilters(): FilterConfig {
    return {
      "general": {
        "my_edits": { 
          "field": "author",
          "pattern": this.state.user, 
        },
        "unreleased": {
          "field": "status",
          "pattern": ConfigStatus.UNRELEASED,
        },
        "upgradable": {
          "field": "status",
          "pattern": ConfigStatus.UPGRADABLE,
        },
      },
    }
  }
}
