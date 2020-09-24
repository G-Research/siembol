import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { AppConfigService } from '../config';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigWrapperService } from './config-wrapper-service';
import { ConfigData, ConfigWrapper, Deployment, GitFiles, PullRequestInfo } from '@model';
import { DeploymentWrapper, EditorResult, ExceptionInfo, TestCaseEvaluation } from '@model/config-model';
import { TestCaseMap, TestCaseWrapper } from '@model/test-case';
import { JSONSchema7 } from 'json-schema';
import { ConfigStoreService } from './config-store.service';
import * as omitEmpty from 'omit-empty';
import { UiMetadataMap } from '../model/ui-metadata-map';

export class ServiceContext {
  metaDataMap: UiMetadataMap;
  configLoader: ConfigLoaderService;
  configWrapper: ConfigWrapperService;
  configStore: ConfigStoreService;
  serviceName: string;
  configSchema: JSONSchema7;
  testCaseSchema: JSONSchema7;
  testSpecificationSchema: JSONSchema7;
  constructor() { }
}

export interface IConfigLoaderService {
  originalSchema;
  getConfigs(): Observable<ConfigWrapper<ConfigData>[]>;
  getConfigsFromFiles(files: any);
  getSchema(): Observable<JSONSchema7>;
  getPullRequestStatus(): Observable<PullRequestInfo>;
  getRelease(): Observable<DeploymentWrapper>;
  validateConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<ExceptionInfo>>;
  validateRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  submitConfig(config: ConfigWrapper<ConfigData>): Observable<ConfigWrapper<ConfigData>[]>;
  submitNewConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitConfigEdit(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  testDeploymentConfig(config: any): Observable<EditorResult<any>>;
  testSingleConfig(config: any, testSpecification: any): Observable<EditorResult<any>>;
  getTestSpecificationSchema(): Observable<any>;
  getTestCases(): Observable<TestCaseMap>;
  submitTestCaseEdit(testCase: TestCaseWrapper): Observable<TestCaseMap>;
  submitNewTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap>;
};

@Injectable({
  providedIn: 'root',
})
export class EditorService {
  private serviceContext: ServiceContext = new ServiceContext();
  private serviceNameSubject = new BehaviorSubject<string>(null);

  public get metaDataMap() { return this.serviceContext.metaDataMap; }
  public get configLoader() { return this.serviceContext.configLoader; }
  public get configWrapper() { return this.serviceContext.configWrapper; }
  public get configStore() { return this.serviceContext.configStore; }
  public get serviceName() { return this.serviceContext.serviceName; }
  public get configSchema() { return this.serviceContext.configSchema; }
  public get testCaseSchema() { return this.serviceContext.testCaseSchema; }
  public get testSpecificationSchema() { return this.serviceContext.testSpecificationSchema; }

  public serviceName$ = this.serviceNameSubject.asObservable();
  public user: string;

  constructor(
    private http: HttpClient,
    private config: AppConfigService) {
    this.user = this.config.getUser();
  }

  public setServiceContext(serviceContext: ServiceContext): boolean {
    this.serviceContext = serviceContext;
    this.serviceNameSubject.next(this.serviceName);
    return true;
  }

  public createServiceContext(serviceName: string): Observable<ServiceContext> {
    const metaDataMap = this.config.getUiMetadata(serviceName);
    const configWrapper = new ConfigWrapperService(metaDataMap);
    const configLoader = new ConfigLoaderService(this.http, this.config, serviceName, configWrapper);
    const configStore = new ConfigStoreService(serviceName, this.config, configLoader);
    const testSpecificationFun = metaDataMap.testing.perConfigTestEnabled
      ? configLoader.getTestSpecificationSchema() : Observable.of({});
    const testCaseMapFun = metaDataMap.testing.testCaseEnabled
      ? configLoader.getTestCases() : Observable.of({});

    return Observable.forkJoin([
      configLoader.getConfigs(),
      configLoader.getRelease(),
      configLoader.getSchema(),
      testCaseMapFun,
      testSpecificationFun])
      .map(([configs, deployment, configSchema, testCaseMap, testSpecSchema]) => {
        if (configs && deployment && configSchema && testCaseMap && testSpecSchema) {
          configStore.initialise(configs, deployment, testCaseMap);
          return {
            metaDataMap: metaDataMap,
            configLoader: configLoader,
            configWrapper: configWrapper,
            configStore: configStore,
            serviceName: serviceName,
            configSchema: configSchema,
            testCaseSchema: this.config.getTestCaseSchema(),
            testSpecificationSchema: testSpecSchema
          };
        } else {
          throwError('Can not load service');
        }
      });;
  }

  public cleanConfigData(configData: ConfigData): ConfigData {
    let cfg = this.configWrapper.produceOrderedJson(configData, '/');
    cfg = omitEmpty(cfg);
    return cfg;
  }

  cleanConfig(config: ConfigWrapper<ConfigData>): ConfigWrapper<ConfigData> {
    if (config.isNew) {
      config.configData[this.metaDataMap.name] = config.name;
      config.configData[this.metaDataMap.version] = config.version = 0;
      config.configData[this.metaDataMap.author] = config.author = this.user;
    } else {
      config.configData[this.metaDataMap.name] = config.name;
      config.configData[this.metaDataMap.version] = config.version;
      config.configData[this.metaDataMap.author] = config.author;
    }

    config.description = config.configData[this.metaDataMap.description];
    config.configData = this.cleanConfigData(config.configData);
    return config;
  }
}
