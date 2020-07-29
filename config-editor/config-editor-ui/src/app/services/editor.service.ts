import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppConfigService } from '../config';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigWrapperService } from './config-wrapper-service';
import { ConfigData, ConfigWrapper, Deployment, GitFiles, PullRequestInfo, RepositoryLinks, RepositoryLinksWrapper } from '@model';
import { ConfigTestDto, DeploymentWrapper, EditorResult, ExceptionInfo, SchemaInfo, TestCaseEvaluation } from '@model/config-model';
import { TestCase, TestCaseMap, TestCaseResult, TestCaseWrapper } from '@model/test-case';
import { JSONSchema7 } from 'json-schema';
import { ServiceInfo } from '../model/config-model';

export interface IConfigLoaderService {
  originalSchema;
  getConfigs(): Observable<ConfigWrapper<ConfigData>[]>;
  getConfigsFromFiles(files: any);
  getSchema(): Observable<JSONSchema7>;
  getPullRequestStatus(): Observable<PullRequestInfo>;
  getRelease(): Observable<DeploymentWrapper>;
  validateConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<ExceptionInfo>>;
  validateRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  submitNewConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitConfigEdit(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  testDeploymentConfig(config: any): Observable<EditorResult<any>>;
  testSingleConfig(config: ConfigTestDto): Observable<EditorResult<any>>;
  getTestSpecificationSchema(): Observable<any>;
  getTestCases(): Observable<TestCaseMap>;
  submitTestCaseEdit(testCase: TestCaseWrapper): Observable<TestCaseMap>;
  submitNewTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap>;
};

export function replacer(key, value) {
    return value === null ? undefined : value;
}

@Injectable({
    providedIn: 'root',
  })
export class EditorService {

  loaderServices: Map<string, IConfigLoaderService> = new Map();
  public configLoader: ConfigLoaderService;
    configWrapper: ConfigWrapperService;

  constructor(
    private http: HttpClient,
    private config: AppConfigService) {}

  public getUser(): Observable<string> {
    return Observable.of(this.config.getUser());
  }

  public getServiceNames(): Observable<string[]> {
    return Observable.of(this.config.getServiceNames());
  }

  public getRepositoryLinks(serviceName): Observable<RepositoryLinks> {
    return this.http.get<EditorResult<RepositoryLinksWrapper>>(
      `${this.config.serviceRoot}api/v1/${serviceName}/configstore/repositories`).pipe(
        map(result => ({
            ...result.attributes.rules_repositories,
            service_name: serviceName,
        }))
      )
  }

  public createLoader(serviceName: string) {
    this.configWrapper = new ConfigWrapperService(this.config.getUiMetadata(serviceName));
    this.configLoader = new ConfigLoaderService(this.http, this.config, serviceName, this.configWrapper);
  }

  public getTestCaseSchema(): Observable<any> {
    return Observable.of(this.config.getTestCaseSchema());
  }

  public validateTestCase(testcase: TestCase): Observable<EditorResult<ExceptionInfo>> {
    const outObj = {
        files: [{
            content: testcase,
        }],
    }
    const json = JSON.parse(JSON.stringify(outObj, replacer, 2));

    return this.http.post<EditorResult<ExceptionInfo>>(`${this.config.serviceRoot}api/v1/testcases/validate`, json);
  }

  public evaluateTestCase(testcase: TestCase, testResult: any): Observable<any> {
    const outObj: TestCaseEvaluation = {
        files: [{
            content: testcase,
        }],
        test_result_raw_output: JSON.stringify(testResult, replacer, 2),
    }
    const headers = new HttpHeaders();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    return this.http.post<EditorResult<TestCaseResult>>(`${this.config.serviceRoot}api/v1/testcases/evaluate`, outObj).pipe(
        map(x => x.attributes)
    )
  }
}
