import { cloneDeep } from 'lodash';
import { BehaviorSubject, Observable, of } from 'rxjs';
import 'rxjs/add/operator/finally';
import { Config, Deployment, PullRequestInfo } from '../../model';
import { ConfigStoreState } from '../../model/store-state';
import { TestCaseMap, TestCaseWrapper } from '../../model/test-case';
import { UiMetadata } from '../../model/ui-metadata-map';
import { ConfigLoaderService } from '../config-loader.service';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { TestStoreService } from './test-store.service';
import { AdminConfig, Type } from '@app/model/config-model';
import { ClipboardStoreService } from '../clipboard-store.service';
import { ConfigHistoryService } from '../config-history.service';

const initialConfigStoreState: ConfigStoreState = {
  adminConfig: undefined,
  configs: [],
  deployment: undefined,
  deploymentHistory: [],
  editedConfig: null,
  editedTestCase: null,
  filterMyConfigs: false,
  filterUndeployed: false,
  filterUpgradable: false,
  filteredConfigs: [],
  filteredDeployment: undefined,
  initialDeployment: undefined,
  releaseSubmitInFlight: false,
  searchTerm: undefined,
  sortedConfigs: [],
  testCaseMap: {},
  pastedConfig: undefined,
};

const initialPullRequestState: PullRequestInfo = {
  pull_request_pending: undefined,
  pull_request_url: undefined,
};

export class ConfigStoreService {
  private readonly store = new BehaviorSubject<ConfigStoreState>(initialConfigStoreState);
  private readonly pullRequestInfo = new BehaviorSubject<PullRequestInfo>(initialPullRequestState);
  private readonly adminPullRequestInfo = new BehaviorSubject<PullRequestInfo>(initialPullRequestState);

  /*eslint-disable */
  public readonly allConfigs$ = this.store.asObservable().map(x => x.configs);
  public readonly deployment$ = this.store.asObservable().map(x => x.deployment);
  public readonly initialDeployment$ = this.store.asObservable().map(x => x.initialDeployment);
  public readonly filteredConfigs$ = this.store.asObservable().map(x => x.filteredConfigs);
  public readonly filteredDeployment$ = this.store.asObservable().map(x => x.filteredDeployment);
  public readonly searchTerm$ = this.store.asObservable().map(x => x.searchTerm);
  public readonly filterMyConfigs$ = this.store.asObservable().map(x => x.filterMyConfigs);
  public readonly filterUndeployed$ = this.store.asObservable().map(x => x.filterUndeployed);
  public readonly filterUpgradable$ = this.store.asObservable().map(x => x.filterUpgradable);
  public readonly deploymentHistory$ = this.store.asObservable().map(x => x.deploymentHistory);
  public readonly editedConfig$ = this.store.asObservable().map(x => x.editedConfig);
  public readonly editedConfigTestCases$ = this.store.asObservable().map(x => x.editedConfig.testCases);
  public readonly editedTestCase$ = this.store.asObservable().map(x => x.editedTestCase);
  public readonly editingTestCase$ = this.store.asObservable().map(x => x.editedTestCase !== null);
  public readonly releaseSubmitInFlight$ = this.store.asObservable().map(x => x.releaseSubmitInFlight);
  public readonly pullRequestPending$ = this.pullRequestInfo.asObservable();
  public readonly adminPullRequestPending$ = this.adminPullRequestInfo.asObservable();
  public readonly adminConfig$ = this.store.asObservable().map(x => x.adminConfig);

  /*eslint-enable */

  private testStoreService: TestStoreService;
  private clipboardStoreService: ClipboardStoreService;
  private configHistoryService: ConfigHistoryService;

  get testService(): TestStoreService {
    return this.testStoreService;
  }

  get clipboardService(): ClipboardStoreService {
    return this.clipboardStoreService;
  }

  constructor(private user: string, private metaDataMap: UiMetadata, private configLoaderService: ConfigLoaderService) {
    this.clipboardStoreService = new ClipboardStoreService(this.configLoaderService, this.store);
    this.testStoreService = new TestStoreService(
      this.user,
      this.store,
      this.configLoaderService,
      this.clipboardStoreService
    );
    this.configHistoryService = new ConfigHistoryService();
  }

  initialise(configs: Config[], deployment: any, testCaseMap: TestCaseMap) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .testCaseMap(testCaseMap)
      .configs(configs)
      .updateTestCasesInConfigs()
      .deployment(deployment.storedDeployment)
      .initialDeployment(deployment.storedDeployment)
      .deploymentHistory(deployment.deploymentHistory)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
    this.loadPullRequestStatus();
  }

  updateAdmin(config: AdminConfig) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue()).adminConfig(config).build();
    this.store.next(newState);
    this.loadAdminPullRequestStatus();
  }

  updateAdminAndHistory(config: AdminConfig) {
    this.configHistoryService.addConfig(cloneDeep(config));
    this.updateAdmin(config);
  }

  updateSearchTerm(searchTerm: string) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .searchTerm(searchTerm)
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  updateFilterMyConfigs(value: boolean) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .filterMyConfigs(value)
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  updateFilterUpgradable(value: boolean) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .filterUpgradable(value)
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  updateFilterUndeployed(value: boolean) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .filterUndeployed(value)
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  addConfigToDeployment(filteredIndex: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .addConfigToDeployment(filteredIndex)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  addConfigToDeploymentInPosition(filteredConfigIndex: number, filteredDeploymentPosition: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .addConfigToDeploymenInPosition(filteredConfigIndex, filteredDeploymentPosition)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  removeConfigFromDeployment(filteredIndex: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .removeConfigFromDeployment(filteredIndex)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  upgradeConfigInDeployment(filteredIndex: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .upgradeConfigInDeployment(filteredIndex)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  moveConfigInDeployment(filteredPreviousIndex: number, filteredCurrentIndex: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .moveConfigInDeployment(filteredPreviousIndex, filteredCurrentIndex)
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
  }

  loadPullRequestStatus() {
    this.configLoaderService.getPullRequestStatus().subscribe((info: PullRequestInfo) => {
      if (info) {
        this.pullRequestInfo.next(info);
      }
    });
  }

  loadAdminPullRequestStatus() {
    this.configLoaderService.getAdminPullRequestStatus().subscribe((info: PullRequestInfo) => {
      if (info) {
        this.adminPullRequestInfo.next(info);
      }
    });
  }

  submitRelease(deployment: Deployment) {
    this.updateReleaseSubmitInFlight(true);
    this.configLoaderService
      .submitRelease(deployment)
      .finally(() => {
        this.updateReleaseSubmitInFlight(false);
      })
      .subscribe((result: any) => {
        if (result) {
          this.loadPullRequestStatus();
        }
      });
  }

  reloadStoreAndDeployment(): Observable<any> {
    const testCaseMapFun = this.metaDataMap.testing.testCaseEnabled ? this.configLoaderService.getTestCases() : of({});
    return Observable.forkJoin(
      this.configLoaderService.getConfigs(),
      this.configLoaderService.getRelease(),
      testCaseMapFun
    ).map(([configs, deployment, testCaseMap]) => {
      if (configs && deployment && testCaseMap) {
        this.initialise(configs, deployment, testCaseMap);
      }
    });
  }

  reloadAdminConfig(): Observable<any> {
    return this.configLoaderService.getAdminConfig().map((config: AdminConfig) => {
      this.updateAdmin(config);
      this.configHistoryService.clear();
      this.configHistoryService.addConfig(config);
    });
  }

  validateEditedConfig(): Observable<any> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }

    return this.configLoaderService.validateConfig(config.configData);
  }

  validateAdminConfig(): Observable<any> {
    const config = this.store.getValue().adminConfig;
    if (!config) {
      throw Error('empty admin config');
    }

    return this.configLoaderService.validateAdminConfig(config.configData);
  }

  submitEditedConfig(): Observable<boolean> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }

    return this.configLoaderService.submitConfig(config).map(configs => {
      if (configs) {
        const currentEdited = configs.find(x => x.name === config.name);
        if (!currentEdited) {
          throw Error('Unexpected response from server during submitting config');
        }

        const newState = new ConfigStoreStateBuilder(this.store.getValue())
          .configs(configs)
          .updateTestCasesInConfigs()
          .detectOutdatedConfigs()
          .reorderConfigsByDeployment()
          .computeFiltered(this.user)
          .editedConfigByName(config.name)
          .build();
        this.store.next(newState);
        this.configHistoryService.clear();
        this.configHistoryService.addConfig(config);

        return true;
      }
    });
  }

  submitAdminConfig(): Observable<boolean> {
    const adminConfig = this.store.getValue().adminConfig;
    if (!adminConfig) {
      throw Error('empty admin config');
    }

    return this.configLoaderService.submitAdminConfig(adminConfig).map((result: any) => {
      if (result) {
        this.loadAdminPullRequestStatus();
        this.configHistoryService.clear();
        this.configHistoryService.addConfig(adminConfig);
        return true;
      }
      return false;
    });
  }

  /**
   * Updates config and test case. If config is already loaded it does not overwrite it.
   *
   * @param configName
   * @param testCaseName
   * @returns false if config or test case don't exist, else true
   */
  setEditedConfigAndTestCaseByName(configName: string, testCaseName: string): boolean {
    this.clearConfigHistory();
    const editedConfig = this.store.value.editedConfig;
    const config = editedConfig && configName === editedConfig.name ? editedConfig : this.getConfigByName(configName);
    if (config === undefined) {
      return false;
    }
    if (testCaseName) {
      const testCase = config.testCases.find(x => x.testCase.test_case_name === testCaseName);
      if (testCase === undefined) {
        return false;
      }
      this.updateEditedConfigAndTestCase(config, testCase);
    } else {
      this.updateEditedConfigAndTestCase(config, null);
    }
    return true;
  }

  setEditedClonedConfigByName(configName: string) {
    this.clearConfigHistory();
    const configToClone = this.getConfigByName(configName);
    if (configToClone === undefined) {
      throw Error('no config with such name');
    }
    const cloned = {
      author: this.user,
      configData: Object.assign({}, cloneDeep(configToClone.configData), {
        [this.metaDataMap.name]: `${configToClone.name}_clone`,
        [this.metaDataMap.version]: 0,
      }),
      description: `cloned from ${configToClone.name}`,
      isNew: true,
      name: `${configToClone.name}_clone`,
      savedInBackend: false,
      testCases: [],
      version: 0,
    };
    this.updateEditedConfigAndTestCase(cloned, null);
  }

  setEditedConfigNew() {
    this.clearConfigHistory();
    const currentState = this.store.getValue();
    const newConfig = {
      author: this.user,
      configData: {},
      description: 'no description',
      isNew: true,
      name: `new_entry_${currentState.configs.length}`,
      savedInBackend: false,
      testCases: [],
      version: 0,
    };

    this.updateEditedConfigAndTestCase(newConfig, null);
  }

  setNewEditedPastedConfig() {
    this.clearConfigHistory();
    const currentState = this.store.getValue();
    const configData = currentState.pastedConfig;
    if (!configData) {
      throw Error('No pasted config available');
    }
    const pasted = {
      author: this.user,
      configData: Object.assign({}, cloneDeep(configData), {
        [this.metaDataMap.name]: `new_entry_${currentState.configs.length}`,
        [this.metaDataMap.version]: 0,
        [this.metaDataMap.author]: this.user,
      }),
      description: 'no description',
      isNew: true,
      name: `new_entry_${currentState.configs.length}`,
      savedInBackend: false,
      testCases: [],
      version: 0,
    };
    this.updateEditedConfigAndTestCase(pasted, null);
  }

  setEditedPastedConfig() {
    this.clipboardService.validateConfig(Type.CONFIG_TYPE).subscribe(() => {
      const currentState = this.store.getValue();
      const configData = currentState.pastedConfig;
      const editedConfig = currentState.editedConfig;
      const pastedConfig = cloneDeep(editedConfig);
      pastedConfig.author = this.user;
      pastedConfig.configData = Object.assign({}, cloneDeep(configData), {
        [this.metaDataMap.name]: editedConfig.name,
        [this.metaDataMap.version]: editedConfig.version,
        [this.metaDataMap.author]: this.user,
      });
      this.updateEditedConfig(pastedConfig);
      this.configHistoryService.addConfig(pastedConfig);
    });
  }

  setEditedPastedAdminConfig() {
    this.clipboardService.validateConfig(Type.ADMIN_TYPE).subscribe(() => {
      const currentState = this.store.getValue();
      const configData = currentState.pastedConfig;
      const adminConfig = currentState.adminConfig;
      const pastedConfig = cloneDeep(adminConfig);
      pastedConfig.configData = Object.assign({}, cloneDeep(configData), {
        config_version: adminConfig.version,
      });
      this.updateAdmin(pastedConfig);
      this.configHistoryService.addConfig(pastedConfig);
    });
  }

  updateEditedConfig(config: Config) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue()).editedConfig(config).build();
    this.store.next(newState);
  }

  updateEditedConfigAndHistory(config: Config) {
    this.configHistoryService.addConfig(cloneDeep(config));
    this.updateEditedConfig(config);
  }

  deleteConfig(configName: string): Observable<any> {
    return this.configLoaderService.deleteConfig(configName).map(data => {
      const newState = new ConfigStoreStateBuilder(this.store.getValue())
        .testCaseMap(data.testCases)
        .configs(data.configs)
        .updateTestCasesInConfigs()
        .detectOutdatedConfigs()
        .reorderConfigsByDeployment()
        .computeFiltered(this.user)
        .build();

      this.store.next(newState);
    });
  }

  deleteTestCase(configName: string, testCaseName: string): Observable<any> {
    return this.configLoaderService.deleteTestCase(configName, testCaseName).map(testCaseMap => {
      const newState = new ConfigStoreStateBuilder(this.store.getValue())
        .testCaseMap(testCaseMap)
        .updateTestCasesInConfigs()
        .editedConfigByName(configName)
        .computeFiltered(this.user)
        .build();

      this.store.next(newState);
    });
  }

  undoConfig() {
    const nextState = this.configHistoryService.undoConfig();
    this.updateEditedConfig(nextState.formState);
  }

  redoConfig() {
    const nextState = this.configHistoryService.redoConfig();
    this.updateEditedConfig(nextState.formState);
  }

  undoAdminConfig() {
    const nextState = this.configHistoryService.undoConfig();
    this.updateAdmin(nextState.formState);
  }

  redoAdminConfig() {
    const nextState = this.configHistoryService.redoConfig();
    this.updateAdmin(nextState.formState);
  }

  clearConfigHistory() {
    this.configHistoryService.clear();
    this.testStoreService.testCaseHistoryService.clear();
  }

  private updateReleaseSubmitInFlight(releaseSubmitInFlight: boolean) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .releaseSubmitInFlight(releaseSubmitInFlight)
      .build();
    this.store.next(newState);
  }

  private updateEditedConfigAndTestCase(config: Config, testCase: TestCaseWrapper) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .editedConfig(config)
      .editedTestCase(testCase)
      .build();
    this.store.next(newState);
  }

  private getConfigByName(configName: string): Config {
    const currentState = this.store.getValue();
    const config = currentState.configs.find(x => x.name === configName);

    return cloneDeep(config);
  }
}
