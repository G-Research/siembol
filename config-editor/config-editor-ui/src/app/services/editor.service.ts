import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { AppConfigService } from '../config';
import { ConfigLoaderService } from './config-loader.service';
import { JSONSchema7 } from 'json-schema';
import { ConfigStoreService } from './config-store.service';
import { UiMetadataMap } from '../model/ui-metadata-map';
import { AppService } from './app.service';
import { mergeMap } from 'rxjs/operators';
import { ConfigSchemaService } from './config-schema-service';

export class ServiceContext {
  metaDataMap: UiMetadataMap;
  configLoader: ConfigLoaderService;
  configSchema: ConfigSchemaService;
  configStore: ConfigStoreService;
  serviceName: string;
  testSpecificationSchema: JSONSchema7;
  constructor() { }
}

@Injectable({
  providedIn: 'root',
})
export class EditorService {
  private serviceContext: ServiceContext = new ServiceContext();
  private serviceNameSubject = new BehaviorSubject<string>(null);

  public get metaDataMap() { return this.serviceContext.metaDataMap; }
  public get configLoader() { return this.serviceContext.configLoader; }
  public get configStore() { return this.serviceContext.configStore; }
  public get serviceName() { return this.serviceContext.serviceName; }
  public get configSchema() { return this.serviceContext.configSchema; }

  public get testSpecificationSchema() { return this.serviceContext.testSpecificationSchema; }


  public serviceName$ = this.serviceNameSubject.asObservable();

  constructor(
    private http: HttpClient,
    private config: AppConfigService,
    private appService: AppService) {
  }

  public setServiceContext(serviceContext: ServiceContext): boolean {
    this.serviceContext = serviceContext;
    this.serviceNameSubject.next(this.serviceName);
    return true;
  }

  public createServiceContext(serviceName: string): Observable<ServiceContext> {
    const metaDataMap = this.appService.getUiMetadataMap(serviceName);
    const user = this.appService.user;
    const configLoader = new ConfigLoaderService(this.http, this.config, serviceName, metaDataMap);
    const configStore = new ConfigStoreService(serviceName, user, this.config, configLoader);
    const testSpecificationFun = metaDataMap.testing.perConfigTestEnabled
      ? configLoader.getTestSpecificationSchema() : Observable.of({});
    const testCaseMapFun = metaDataMap.testing.testCaseEnabled
      ? configLoader.getTestCases() : Observable.of({});

    return configLoader.getSchema()
      .pipe(
        mergeMap(schema =>
          Observable.forkJoin(
            configLoader.getConfigs(),
            configLoader.getRelease(),
            Observable.of(schema),
            testCaseMapFun,
            testSpecificationFun))).
      map(([configs, deployment, originalSchema, testCaseMap, testSpecSchema]) => {
        if (configs && deployment && originalSchema && testCaseMap && testSpecSchema) {
          configStore.initialise(configs, deployment, testCaseMap);
          return {
            metaDataMap: metaDataMap,
            configLoader: configLoader,
            configStore: configStore,
            serviceName: serviceName,
            configSchema: new ConfigSchemaService(metaDataMap, user, originalSchema),
            testSpecificationSchema: testSpecSchema
          };
        } else {
          throwError('Can not load service');
        }
      });
  }
}
