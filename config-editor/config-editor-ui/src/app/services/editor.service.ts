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
import { CheckboxEvent, FILTER_PARAM_KEY, ServiceSearchHistory } from '@app/model/config-model';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { SearchHistoryService } from './search-history.service';

export class ServiceContext {
  metaDataMap: UiMetadata;
  configLoader: ConfigLoaderService;
  configSchema?: ConfigSchemaService;
  adminSchema?: AdminSchemaService;
  configStore: ConfigStoreService;
  serviceName: string;
  testSpecificationSchema?: JSONSchema7;
  adminMode: boolean;
  searchHistoryService?: SearchHistoryService;
}

@Injectable({
  providedIn: 'root',
})
export class EditorService {
  private currentParams: ParamMap;
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
  get testSpecificationSchema() {
    return this.serviceContext.testSpecificationSchema;
  }

  get searchHistoryService() {
    return this.serviceContext.searchHistoryService;
  }

  constructor(
    private http: HttpClient, 
    private config: AppConfigService, 
    private appService: AppService,
    private route: ActivatedRoute
  ) {
    this.route.queryParamMap.subscribe(params => {
      this.currentParams = params;
    })
  }

  setServiceContext(serviceContext: ServiceContext): boolean {
    this.serviceContext = serviceContext;
    this.appService.updateServiceContextMap(serviceContext);
    this.serviceNameSubject.next(this.serviceName);
    return true;
  }

  createConfigServiceContext(serviceName: string): Observable<ServiceContext> {
    const [metaDataMap, user, configLoader, configStore] = this.initialiseContext(serviceName);
    const testSpecificationFun = metaDataMap.testing.perConfigTestEnabled
      ? configLoader.getTestSpecificationSchema()
      : of({});
    const testCaseMapFun = metaDataMap.testing.testCaseEnabled ? configLoader.getTestCases() : of({});

    return configLoader
      .getSchema()
      .pipe(
        mergeMap(schema =>
          forkJoin(
            configLoader.getConfigs(),
            configLoader.getRelease(),
            of(schema),
            testCaseMapFun,
            testSpecificationFun
          )
        )
      )
      .pipe(map(([configs, release, originalSchema, testCaseMap, testSpecSchema]) => {
        if (configs && release && originalSchema && testCaseMap && testSpecSchema) {
          configStore.initialise(configs, release, testCaseMap, user, metaDataMap);
          return {
            adminMode: false,
            configLoader,
            configSchema: new ConfigSchemaService(metaDataMap, user, originalSchema),
            configStore,
            metaDataMap,
            serviceName,
            testSpecificationSchema: testSpecSchema,
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

  getLatestFilters(event: CheckboxEvent): any {
    const filters = [];
    this.currentParams.getAll(FILTER_PARAM_KEY).forEach(filter => {
      if (filter !== event.name || event.checked === true) {
        filters.push(filter);
      }
    });
    if (!filters[event.name] && event.checked === true) {
      filters.push(event.name);
    }
    return filters;
  }

  onSaveSearch(): ServiceSearchHistory[] {
    return this.serviceContext.searchHistoryService.addToSearchHistory(this.currentParams);
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
