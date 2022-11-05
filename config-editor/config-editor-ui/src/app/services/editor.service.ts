import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError, BehaviorSubject, of, forkJoin } from 'rxjs';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigLoaderService } from './config-loader.service';
import { JSONSchema7 } from 'json-schema';
import { ConfigStoreService } from './store/config-store.service';
import { UiMetadata } from '../model/ui-metadata-map';
import { AppService } from './app.service';
import { mergeMap, map } from 'rxjs/operators';
import { ConfigSchemaService } from './schema/config-schema-service';
import { AdminSchemaService } from './schema/admin-schema.service';
import { CheckboxEvent, FILTER_PARAM_KEY, ServiceSearch, TestConfigSpec, TestSpecificationTesters } from '@app/model/config-model';
import { SearchHistoryService } from './search-history.service';
import { ParamMap } from '@angular/router';

export class ServiceContext {
  metaDataMap: UiMetadata;
  configLoader: ConfigLoaderService;
  configSchema?: ConfigSchemaService;
  adminSchema?: AdminSchemaService;
  configStore: ConfigStoreService;
  serviceName: string;
  adminMode: boolean;
  searchHistoryService?: SearchHistoryService;
  testConfigSpec?: TestConfigSpec[];
  testSpecificationTesters?: TestSpecificationTesters;
}

@Injectable({
  providedIn: 'root',
})
export class EditorService {
  private serviceContext: ServiceContext = new ServiceContext();
  private serviceNameSubject = new BehaviorSubject<string>(null);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  serviceName$ = this.serviceNameSubject.asObservable();

  get metaDataMap() {
    return this.serviceContext.metaDataMap;
  }
  get configLoader() {
    return this.serviceContext.configLoader;
  }
  get configStore() {
    return this.serviceContext.configStore;
  }
  get serviceName() {
    return this.serviceContext.serviceName;
  }
  get configSchema() {
    return this.serviceContext.configSchema;
  }
  get adminSchema() {
    return this.serviceContext.adminSchema;
  }
  get adminMode() {
    return this.serviceContext.adminMode;
  }
  get searchHistoryService() {
    return this.serviceContext.searchHistoryService;
  }
  get testConfigSpec() {
    return this.serviceContext.testConfigSpec;
  }
  get testSpecificationTesters() {
    return this.serviceContext.testSpecificationTesters;
  }

  constructor(
    private http: HttpClient, 
    private config: AppConfigService, 
    private appService: AppService
  ) {}

  setServiceContext(serviceContext: ServiceContext): boolean {
    this.serviceContext = serviceContext;
    this.appService.updateServiceContextMap(serviceContext);
    this.serviceNameSubject.next(this.serviceName);
    return true;
  }

  createConfigServiceContext(serviceName: string): Observable<ServiceContext> {
    const [metaDataMap, user, configLoader, configStore] = this.initialiseContext(serviceName);  

    return forkJoin(configLoader.getSchema(), configLoader.getConfigTesters())
      .pipe(mergeMap(([schema, testConfig]) => {
        let testers: TestSpecificationTesters = this.getTestersPerType(testConfig);
        return forkJoin(
          configLoader.getConfigs(),
          configLoader.getRelease(),
          of(schema),
          of(testConfig),
          testers.config_testing.length > 0 ? configLoader.getTestCases() : of({}),
          of(testers)
        )
      }))
      .pipe(map(([configs, release, originalSchema, testSpec, testCaseMap, testersType]) => {
        if (configs && release && originalSchema && testSpec && testCaseMap && testersType) {
          
          configStore.initialise(configs, release, testCaseMap, user, metaDataMap, testersType);
          return {
            adminMode: false,
            configLoader,
            configSchema: new ConfigSchemaService(metaDataMap, user, originalSchema),
            configStore,
            metaDataMap,
            serviceName,
            testConfigSpec: testSpec,
            testSpecificationTesters: testersType,
            searchHistoryService: new SearchHistoryService(this.config, serviceName),
          };
        }
        throwError(() => 'Can not load service');
      }));
  }

  createAdminServiceContext(serviceName: string): Observable<ServiceContext> {
    const [metaDataMap, user, configLoader, configStore] = this.initialiseContext(serviceName);

    return configLoader
      .getAdminSchema()
      .pipe(
        mergeMap(schema => forkJoin([configLoader.getAdminConfig(), of(schema)])),
        map(([adminConfig, originalSchema]) => {
        if (adminConfig && originalSchema) {
          configStore.updateAdmin(adminConfig);
          return {
            adminMode: true,
            adminSchema: new AdminSchemaService(metaDataMap, user, originalSchema),
            configLoader,
            configStore,
            metaDataMap,
            serviceName,
          };
        }
        throwError(() => 'Can not load admin service');
      }));
  }

  getLatestFilters(event: CheckboxEvent, currentParams: ParamMap): any {
    const filters = [];
    currentParams.getAll(FILTER_PARAM_KEY).forEach(filter => {
      if (filter !== event.name || event.checked === true) {
        filters.push(filter);
      }
    });
    if (!filters[event.name] && event.checked === true) {
      filters.push(event.name);
    }
    return filters;
  }

  getTestConfig(name: string): TestConfigSpec {
    return this.testConfigSpec.find(x => x.name === name); 
  }

  getTestersPerType(testConfig: TestConfigSpec[]): TestSpecificationTesters {
    let testers: TestSpecificationTesters = {config_testing: [], test_case_testing: [], release_testing: []};
    testConfig.forEach((tester: TestConfigSpec) => {
      if (tester.config_testing) {
        testers.config_testing.push(tester.name);
      }
      if (tester.test_case_testing) {
        testers.test_case_testing.push(tester.name);
      }
      if (tester.release_testing) {
        testers.release_testing.push(tester.name);
      }
     });
     return testers;
  }

  onSaveSearch(currentParams: ParamMap): ServiceSearch[] {
    return this.serviceContext.searchHistoryService.addToSearchHistory(currentParams);
  }

  onDeleteSearch(search: ServiceSearch): ServiceSearch[] {
    return this.serviceContext.searchHistoryService.deleteSavedSearch(search);
  }

  private initialiseContext(
    serviceName: string
  ): [UiMetadata, string, ConfigLoaderService, ConfigStoreService] {
    const metaDataMap = this.appService.getUiMetadataMap(serviceName);
    const user = this.appService.user;
    const configLoader = new ConfigLoaderService(this.http, this.config, serviceName, metaDataMap);
    const configStore = new ConfigStoreService(user, metaDataMap, configLoader, this.config, this.appService);
    return [metaDataMap, user, configLoader, configStore];
  }
}
