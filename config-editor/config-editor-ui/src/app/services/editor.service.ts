import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigLoaderService } from './config-loader.service';
import { JSONSchema7 } from 'json-schema';
import { ConfigStoreService } from './store/config-store.service';
import { UiMetadata } from '../model/ui-metadata-map';
import { AppService } from './app.service';
import { mergeMap } from 'rxjs/operators';
import { ConfigSchemaService } from './schema/config-schema-service';
import { RepositoryLinks } from '@app/model/config-model';
import { AdminSchemaService } from './schema/admin-schema.service';

export class ServiceContext {
  metaDataMap: UiMetadata;
  configLoader: ConfigLoaderService;
  configSchema?: ConfigSchemaService;
  adminSchema?: AdminSchemaService;
  configStore: ConfigStoreService;
  serviceName: string;
  testSpecificationSchema?: JSONSchema7;
  adminMode: boolean;
  repositoryLinks$: Observable<RepositoryLinks>;
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
  get repositoryLinks$() {
    return this.serviceContext.repositoryLinks$;
  }
  get testSpecificationSchema() {
    return this.serviceContext.testSpecificationSchema;
  }

  constructor(private http: HttpClient, private config: AppConfigService, private appService: AppService) {}

  setServiceContext(serviceContext: ServiceContext): boolean {
    this.serviceContext = serviceContext;
    this.serviceNameSubject.next(this.serviceName);
    return true;
  }

  createConfigServiceContext(serviceName: string): Observable<ServiceContext> {
    const [metaDataMap, user, configLoader, configStore, repositoryLinks$] = this.initialiseContext(serviceName);
    const testSpecificationFun = metaDataMap.testing.perConfigTestEnabled
      ? configLoader.getTestSpecificationSchema()
      : of({});
    const testCaseMapFun = metaDataMap.testing.testCaseEnabled ? configLoader.getTestCases() : of({});

    return configLoader
      .getSchema()
      .pipe(
        mergeMap(schema =>
          Observable.forkJoin(
            configLoader.getConfigs(),
            configLoader.getRelease(),
            of(schema),
            testCaseMapFun,
            testSpecificationFun
          )
        )
      )
      .map(([configs, deployment, originalSchema, testCaseMap, testSpecSchema]) => {
        if (configs && deployment && originalSchema && testCaseMap && testSpecSchema) {
          configStore.initialise(configs, deployment, testCaseMap);
          return {
            adminMode: false,
            configLoader,
            configSchema: new ConfigSchemaService(metaDataMap, user, originalSchema),
            configStore,
            metaDataMap,
            repositoryLinks$,
            serviceName,
            testSpecificationSchema: testSpecSchema,
          };
        }
        throwError('Can not load service');
      });
  }

  createAdminServiceContext(serviceName: string): Observable<ServiceContext> {
    const [metaDataMap, user, configLoader, configStore, repositoryLinks$] = this.initialiseContext(serviceName);

    return configLoader
      .getAdminSchema()
      .pipe(mergeMap(schema => Observable.forkJoin(configLoader.getAdminConfig(), of(schema))))
      .map(([adminConfig, originalSchema]) => {
        if (adminConfig && originalSchema) {
          configStore.updateAdmin(adminConfig);
          return {
            adminMode: true,
            adminSchema: new AdminSchemaService(metaDataMap, user, originalSchema),
            configLoader,
            configStore,
            metaDataMap,
            repositoryLinks$,
            serviceName,
          };
        }
        throwError('Can not load admin service');
      });
  }

  private initialiseContext(
    serviceName: string
  ): [UiMetadata, string, ConfigLoaderService, ConfigStoreService, Observable<RepositoryLinks>] {
    const metaDataMap = this.appService.getUiMetadataMap(serviceName);
    const user = this.appService.user;
    const configLoader = new ConfigLoaderService(this.http, this.config, serviceName, metaDataMap);
    const configStore = new ConfigStoreService(user, metaDataMap, configLoader);
    const repositoryLinks$ = this.appService.getRepositoryLinks(serviceName);
    return [metaDataMap, user, configLoader, configStore, repositoryLinks$];
  }
}
