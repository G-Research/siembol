import { cloneDeep } from 'lodash';
import { BehaviorSubject, Observable } from 'rxjs';
import 'rxjs/add/operator/finally';
import { AppConfigService } from '../config/app-config.service';
import { ConfigData, ConfigWrapper, Deployment, PullRequestInfo } from '../model';
import { ConfigStoreState } from '../model/store-state';
import { TestCaseMap, TestCaseWrapper } from '../model/test-case';
import { UiMetadataMap } from '../model/ui-metadata-map';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { TestStoreService } from './test-store.service';

const initialConfigStoreState: ConfigStoreState = {
  configs: [],
  deployment: undefined,
  deploymentHistory: [],
  sortedConfigs: [],
  filteredConfigs: [],
  filteredDeployment: undefined,
  searchTerm: undefined,
  filterMyConfigs: false,
  filterUndeployed: false,
  filterUpgradable: false,
  releaseSubmitInFlight: false,
  editedConfig: null,
  testCaseMap: {},
  editedTestCase: null,
}

const initialPullRequestState: PullRequestInfo = {
  pull_request_pending: undefined,
  pull_request_url: undefined
}

export class ConfigStoreService {
  private metaDataMap: UiMetadataMap;
  private testStoreService: TestStoreService;
  private readonly store = new BehaviorSubject<ConfigStoreState>(initialConfigStoreState);
  private readonly pullRequestInfo = new BehaviorSubject<PullRequestInfo>(initialPullRequestState);

  public readonly allConfigs$ = this.store.asObservable().map(x => x.configs);
  public readonly deployment$ = this.store.asObservable().map(x => x.deployment);
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

  public get testService(): TestStoreService { return this.testStoreService; }

  constructor(
    private serviceName: string,
    private user: string,
    private config: AppConfigService,
    private configLoaderService: ConfigLoaderService) {
    this.metaDataMap = config.uiMetadata[serviceName];
    this.testStoreService = new TestStoreService(this.user, this.store, this.configLoaderService);
  }

  initialise(configs: ConfigWrapper<ConfigData>[],
    deployment: any,
    testCaseMap: TestCaseMap) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .testCaseMap(testCaseMap)
      .configs(configs)
      .updateTestCasesInConfigs()
      .deployment(deployment.storedDeployment)
      .deploymentHistory(deployment.deploymentHistory)
      .detectOutdatedConfigs()
      .reorderConfigsByDeployment()
      .computeFiltered(this.user)
      .build();

    this.store.next(newState);
    this.loadPullRequestStatus();
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
    })
  }

  submitRelease(deployment: Deployment<ConfigWrapper<ConfigData>>) {
    this.updateReleaseSubmitInFlight(true);
    this.configLoaderService.submitRelease(deployment)
      .finally(() => {
        this.updateReleaseSubmitInFlight(false);
      })
      .subscribe((result: any) => {
      if (result) {
        this.loadPullRequestStatus();
      }
    })
  }

  validateEditedConfig(): Observable<any> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config')
    }

    return this.configLoaderService.validateConfig(config);
  }

  submitEditedConfig(): Observable<boolean> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config')
    }

    return this.configLoaderService.submitConfig(config)
      .map(configs => {
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

          return true;
        }
      });
  }

  /**
  * Updates config and test case. If config is already loaded it does not overwrite it.
  * @param configName
  * @param testCaseName
  * @returns false if config or test case don't exist, else true
  */
  setEditedConfigAndTestCaseByName(configName: string, testCaseName: string): boolean {
    const editedConfig = this.store.value.editedConfig;
    const config = (editedConfig && configName === editedConfig.name) ?
      editedConfig : this.getConfigByName(configName);
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
    const configToClone = this.getConfigByName(configName);
    if (configToClone === undefined) {
      throw Error("no config with such name");
    }
    const cloned = {
      isNew: true,
      configData: Object.assign({}, cloneDeep(configToClone.configData), {
        [this.metaDataMap.name]: `${configToClone.name}_clone`,
        [this.metaDataMap.version]: 0,
      }),
      savedInBackend: false,
      name: `${configToClone.name}_clone`,
      author: this.user,
      version: 0,
      description: `cloned from ${configToClone.name}`,
      testCases: [],
    };
    this.updateEditedConfig(cloned);
  }

  setEditedConfigNew() {
    const currentState = this.store.getValue();
    const newConfig = {
      isNew: true,
      configData: {},
      savedInBackend: false,
      name: `new_entry_${currentState.configs.length}`,
      version: 0,
      description: 'no description',
      author: this.user,
      testCases: [],
    };

    this.updateEditedConfig(newConfig);
  }

  private updateReleaseSubmitInFlight(releaseSubmitInFlight: boolean) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .releaseSubmitInFlight(releaseSubmitInFlight)
      .build();
    this.store.next(newState);
  }

  updateEditedConfig(config: ConfigWrapper<ConfigData>) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .editedConfig(config)
      .build();
    this.store.next(newState);
  }

  private updateEditedConfigAndTestCase(config: ConfigWrapper<ConfigData>, testCase: TestCaseWrapper) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue())
      .editedConfig(config)
      .editedTestCase(testCase)
      .build();
    this.store.next(newState);
  }

  private getConfigByName(configName: string): ConfigWrapper<ConfigData> {
    const currentState = this.store.getValue();
    const config = currentState.configs.find(x => x.name === configName);

    return cloneDeep(config);
  }

}
