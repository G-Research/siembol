import { cloneDeep } from 'lodash';
import { BehaviorSubject, forkJoin, Observable, of, mergeMap, map, finalize, throwError } from 'rxjs';
import { Config, Release, PullRequestInfo } from '../../model';
import { ConfigStoreState } from '../../model/store-state';
import { TestCaseMap, TestCaseWrapper } from '../../model/test-case';
import { UiMetadata } from '../../model/ui-metadata-map';
import { ConfigLoaderService } from '../config-loader.service';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { TestStoreService } from './test-store.service';
import { AdminConfig, ConfigAndTestsToClone, ConfigToImport, ExistingConfigError, Importers, Type } from '@app/model/config-model';
import { ClipboardStoreService } from '../clipboard-store.service';
import { ConfigHistoryService } from '../config-history.service';
import { AppConfigService } from '../app-config.service';
import { AppService } from '../app.service';
import { ParamMap } from '@angular/router';

const initialConfigStoreState: ConfigStoreState = {
  adminConfig: undefined,
  configs: [],
  release: undefined,
  releaseHistory: [],
  editedConfig: null,
  editedTestCase: null,
  initialRelease: undefined,
  releaseSubmitInFlight: false,
  searchTerm: undefined,
  sortedConfigs: [],
  testCaseMap: {},
  pastedConfig: undefined,
  countChangesInRelease: 0,
  configManagerRowData: [],
  serviceFilters: [],
  isAnyFilterPresent: false,
  serviceFilterConfig: undefined,
  user: undefined,
};

const initialPullRequestState: PullRequestInfo = {
  pull_request_pending: undefined,
  pull_request_url: undefined,
};

const importers: Importers = {
  config_importers: [],
};

export class ConfigStoreService {
  private readonly store = new BehaviorSubject<ConfigStoreState>(initialConfigStoreState);
  private readonly pullRequestInfo = new BehaviorSubject<PullRequestInfo>(initialPullRequestState);
  private readonly adminPullRequestInfo = new BehaviorSubject<PullRequestInfo>(initialPullRequestState);
  private readonly importers = new BehaviorSubject<Importers>(importers);

  /*eslint-disable */
  public readonly allConfigs$ = this.store.asObservable().pipe(map(x => x.configs));
  public readonly sortedConfigs$ = this.store.asObservable().pipe(map(x => x.sortedConfigs));
  public readonly release$ = this.store.asObservable().pipe(map(x => x.release));
  public readonly initialRelease$ = this.store.asObservable().pipe(map(x => x.initialRelease));
  public readonly searchTerm$ = this.store.asObservable().pipe(map(x => x.searchTerm));
  public readonly releaseHistory$ = this.store.asObservable().pipe(map(x => x.releaseHistory));
  public readonly editedConfig$ = this.store.asObservable().pipe(map(x => x.editedConfig));
  public readonly editedConfigTestCases$ = this.store.asObservable().pipe(map(x => x.editedConfig.testCases));
  public readonly editedTestCase$ = this.store.asObservable().pipe(map(x => x.editedTestCase));
  public readonly editingTestCase$ = this.store.asObservable().pipe(map(x => x.editedTestCase !== null));
  public readonly releaseSubmitInFlight$ = this.store.asObservable().pipe(map(x => x.releaseSubmitInFlight));
  public readonly pullRequestPending$ = this.pullRequestInfo.asObservable();
  public readonly adminPullRequestPending$ = this.adminPullRequestInfo.asObservable();
  public readonly adminConfig$ = this.store.asObservable().pipe(map(x => x.adminConfig));
  public readonly importers$ = this.importers.asObservable();
  public readonly configManagerRowData$ = this.store.asObservable().pipe(map(x => x.configManagerRowData));
  public readonly countChangesInRelease$ = this.store.asObservable().pipe(map(x => x.countChangesInRelease));
  public readonly isAnyFilterPresent$ = this.store.asObservable().pipe(map(x => x.isAnyFilterPresent));
  public readonly serviceFilterConfig$ = this.store.asObservable().pipe(map(x => x.serviceFilterConfig));
  public readonly serviceFilters$ = this.store.asObservable().pipe(map(x => x.serviceFilters));

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

  constructor(
    private user: string, 
    private metaDataMap: UiMetadata, 
    private configLoaderService: ConfigLoaderService, 
    private configService: AppConfigService,
    private appService: AppService
  ) {
    this.clipboardStoreService = new ClipboardStoreService(this.configLoaderService, this.store);
    this.testStoreService = new TestStoreService(
      this.user,
      this.store,
      this.configLoaderService,
      this.clipboardStoreService
    );
    this.configHistoryService = new ConfigHistoryService();
  }

  initialise(
    configs: Config[], 
    release: any, 
    testCaseMap: TestCaseMap, 
    user: string,
    uiMetadata: UiMetadata
  ) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .user(user)
      .serviceFilterConfig(uiMetadata)
      .testCaseMap(testCaseMap)
      .configs(configs)
      .updateTestCasesInConfigs()
      .release(release.storedRelease)
      .initialRelease(release.storedRelease)
      .releaseHistory(release.releaseHistory)
      .detectOutdatedConfigs()
      .reorderConfigsByRelease()
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
    this.loadPullRequestStatus();
    if (this.configService.useImporters) {
      this.loadImporters();
    }
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
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
  }

  addConfigToRelease(name: string) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .addConfigToRelease(name)
      .detectOutdatedConfigs()
      .reorderConfigsByRelease()
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
  }

  removeConfigFromRelease(name: string) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .removeConfigFromRelease(name)
      .detectOutdatedConfigs()
      .reorderConfigsByRelease()
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
  }

  upgradeConfigInRelease(name: string) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .upgradeConfigInRelease(name)
      .detectOutdatedConfigs()
      .reorderConfigsByRelease()
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
  }

  moveConfigInRelease(configName: string, filteredCurrentIndex: number) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .moveConfigInRelease(configName, filteredCurrentIndex)
      .reorderConfigsByRelease()
      .computeConfigManagerRowData()
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

  submitRelease(release: Release) {
    this.updateReleaseSubmitInFlight(true);
    this.configLoaderService
      .submitRelease(release)
      .pipe(finalize(() => {
        this.updateReleaseSubmitInFlight(false);
      }))
      .subscribe((result: any) => {
        if (result) {
          this.loadPullRequestStatus();
        }
      });
  }

  reloadStoreAndRelease(): Observable<any> {
    const testCaseMapFun = this.metaDataMap.testing.testCaseEnabled ? this.configLoaderService.getTestCases() : of({});
    return forkJoin(
      this.configLoaderService.getConfigs(),
      this.configLoaderService.getRelease(),
      testCaseMapFun
    ).pipe(map(([configs, release, testCaseMap]) => {
      if (configs && release && testCaseMap) {
        this.initialise(configs, release, testCaseMap, this.user, this.metaDataMap);
      }
    }));
  }

  reloadAdminConfig(): Observable<any> {
    return this.configLoaderService.getAdminConfig().pipe(map((config: AdminConfig) => {
      this.updateAdmin(config);
      this.configHistoryService.clear();
      this.configHistoryService.addConfig(config);
    }));
  }

  validateEditedConfig(): Observable<any> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }
    return this.validateConfig(config);
  }

  validateConfig(config: Config): Observable<any> {
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

    return this.configLoaderService.submitConfig(config).pipe(map(configs => {
      if (configs) {
        const currentEdited = configs.find(x => x.name === config.name);
        if (!currentEdited) {
          throw Error('Unexpected response from server during submitting config');
        }

        const newState = new ConfigStoreStateBuilder(this.store.getValue())
          .configs(configs)
          .updateTestCasesInConfigs()
          .detectOutdatedConfigs()
          .reorderConfigsByRelease()
          .editedConfigByName(config.name)
          .computeConfigManagerRowData()
          .build();
        this.store.next(newState);
        this.configHistoryService.clear();
        this.configHistoryService.addConfig(config);

        return true;
      }
    }));
  }

  submitAdminConfig(): Observable<boolean> {
    const adminConfig = this.store.getValue().adminConfig;
    if (!adminConfig) {
      throw Error('empty admin config');
    }

    return this.configLoaderService.submitAdminConfig(adminConfig).pipe(map((result: any) => {
      if (result) {
        this.loadAdminPullRequestStatus();
        this.configHistoryService.clear();
        this.configHistoryService.addConfig(adminConfig);
        return true;
      }
      return false;
    }));
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
        [this.metaDataMap.version]: 0,
        [this.metaDataMap.author]: this.user,
      }),
      description: 'no description',
      isNew: true,
      name: configData[this.metaDataMap.name],
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
      pastedConfig.description = configData[this.metaDataMap.description];
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
    return this.configLoaderService.deleteConfig(configName).pipe(map(data => {
      const newState = new ConfigStoreStateBuilder(this.store.getValue())
        .testCaseMap(data.testCases)
        .configs(data.configs)
        .updateTestCasesInConfigs()
        .detectOutdatedConfigs()
        .reorderConfigsByRelease()
        .computeConfigManagerRowData()
        .build();

      this.store.next(newState);
    }));
  }

  deleteTestCase(configName: string, testCaseName: string): Observable<any> {
    return this.configLoaderService.deleteTestCase(configName, testCaseName).pipe(map(testCaseMap => {
      const newState = new ConfigStoreStateBuilder(this.store.getValue())
        .testCaseMap(testCaseMap)
        .updateTestCasesInConfigs()
        .editedConfigByName(configName)
        .build();

      this.store.next(newState);
    }));
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

  importConfig(configToImport: ConfigToImport): Observable<boolean> {
    return this.configLoaderService.importConfig(configToImport).pipe(map(result => {
      if (result.imported_configuration) {
        this.clipboardService.updatePastedConfig(result.imported_configuration);
        return true;
      }
      return false;
    }));
  }

  loadImporters() {
    this.configLoaderService.getImporters().subscribe((i: Importers) => {
      if (i) {
        this.importers.next(i);
      }
    });
  }

  setClonedConfigAndTests(configName: string, newName: string, withTests: boolean): Observable<boolean> {
    const toClone = this.getClonedConfigAndTestsByName(configName, newName, withTests);
    return this.validateAndSubmitClonedConfigAndTests(toClone);
  }

  setClonedConfigAndTestsFromOtherService(
    configName: string, newName: string, withTests: boolean, fromService: string
  ): Observable<boolean> {
    const toClone = this.appService.getServiceContext(fromService)
      .configStore.getClonedConfigAndTestsByName(configName, newName, withTests);
    return this.validateAndSubmitClonedConfigAndTests(toClone);
  }

  getClonedConfigAndTestsByName(configName: string, newName: string, withTests: boolean): ConfigAndTestsToClone {
    const cloned_config = this.getClonedConfig(configName, newName);
    let cloned_test_cases = [];
    if (withTests) {
      const currentState = this.store.getValue();
      const testCaseMap = currentState.testCaseMap;
      if (configName in testCaseMap) {
        cloned_test_cases = testCaseMap[configName].map(
          testCaseWrapper => 
            this.testStoreService.getClonedTestCase(testCaseWrapper, newName)
        );
      }
    }
    return {config: cloned_config, test_cases: cloned_test_cases};
  }

  validateClonedConfigAndTests(toClone: ConfigAndTestsToClone): Observable<boolean> {
    return this.validateConfig(toClone.config)
      .pipe(mergeMap(() => this.testStoreService.validateTestCases(toClone.test_cases)));
  }

  submitClonedConfigAndTests(toClone: ConfigAndTestsToClone): Observable<boolean> {
    return this.configLoaderService.submitConfig(toClone.config)
      .pipe(
        mergeMap(configs => 
          forkJoin([
            of(toClone.config),
            of(configs),
            this.testStoreService.submitTestCases(toClone.test_cases),
          ])
        ),
        map(([config, configs, testCaseMap]) => 
        this.setConfigAndTestCasesInStore(config.name, configs, testCaseMap)
      ));
  }

  validateAndSubmitClonedConfigAndTests(toClone: ConfigAndTestsToClone): Observable<boolean> {
    if (this.getConfigByName(toClone.config.name) === undefined) {
      return this.validateClonedConfigAndTests(toClone).pipe(
        mergeMap(() => this.submitClonedConfigAndTests(toClone))
      );
    }
    return throwError(() => new ExistingConfigError(`Config with name ${toClone.config.name} already exists in service`));
    
  }

  setConfigAndTestCasesInStore(editedConfigName: string, configs: Config[], testCaseMap: TestCaseMap): boolean {
    if (configs) {
      const currentState = this.store.getValue();
      if (!testCaseMap) {
        testCaseMap = currentState.testCaseMap;
      }
      const newState = new ConfigStoreStateBuilder(currentState)
        .editedConfig(configs.find(config => config.name === editedConfigName))
        .configs(configs)
        .testCaseMap(testCaseMap)
        .updateTestCasesInConfigs()
        .detectOutdatedConfigs()
        .reorderConfigsByRelease()
        .editedConfigTestCases(testCaseMap[editedConfigName])
        .computeConfigManagerRowData()
        .build();
      this.store.next(newState);
      return true;
    }
  }

  incrementChangesInRelease() {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .incrementChangesInRelease()
      .build();
    this.store.next(newState);
  }

  resetChangesInRelease() {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .resetChangesInRelease()
      .build();
    this.store.next(newState);
  }

  updateSearchTermAndFilters(params: ParamMap) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .searchTerm(params.get("searchTerm"))
      .updateServiceFilters(params)   
      .computeConfigManagerRowData()
      .build();

    this.store.next(newState);
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

  private getClonedConfig(configName: string, newName: string): Config {
    const configToClone = this.getConfigByName(configName);
    if (configToClone === undefined) {
      throw Error('no config with such name');
    }
    return {
      author: this.user,
      configData: Object.assign({}, cloneDeep(configToClone.configData), {
        [this.metaDataMap.name]: newName,
        [this.metaDataMap.version]: 0,
      }),
      description: configToClone.description,
      isNew: true,
      name: newName,
      savedInBackend: false,
      testCases: [],
      version: 0,
    };
  }
}
