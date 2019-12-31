import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from './config';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigData, ConfigWrapper, Deployment, GitFiles, PullRequestInfo, RepositoryLinks, SchemaDto, SensorFields,
    SensorFieldTemplate, UserName } from './model';
import { ConfigTestDto, DeploymentWrapper, EditorResult, ExceptionInfo, SchemaInfo, TestCaseEvaluation } from './model/config-model';
import { Field } from './model/sensor-fields';
import { TestCase, TestCaseMap, TestCaseResult, TestCaseWrapper } from './model/test-case';
import { StripSuffixPipe } from './pipes';

export interface IConfigLoaderService {
  originalSchema;
  getConfigs(): Observable<ConfigWrapper<ConfigData>[]>;
  getConfigsFromFiles(files: any);
  getSchema(): Observable<SchemaDto>;
  getPullRequestStatus(): Observable<PullRequestInfo>;
  getRelease(): Observable<DeploymentWrapper>;
  getRepositoryLinks(): Observable<RepositoryLinks>;
  validateConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<ExceptionInfo>>;
  validateRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  submitNewConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitConfigEdit(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>>;
  getFields(): Observable<Field[]>
  testDeploymentConfig(config: any): Observable<EditorResult<any>>;
  testSingleConfig(config: ConfigTestDto): Observable<EditorResult<any>>;
  getTestSpecificationSchema(): Observable<any>;
  getTestCases(): Observable<TestCaseMap>;
  submitTestCaseEdit(testCase: TestCaseWrapper): Observable<EditorResult<GitFiles<ConfigData>>>;
  submitNewTestCase(testCase: TestCaseWrapper): Observable<EditorResult<GitFiles<ConfigData>>>;
  produceOrderedJson(configData: ConfigData, path: string);
  unwrapOptionalsFromArrays(obj: any);
};

export function replacer(key, value) {
    return value === null ? undefined : value;
}

@Injectable({
    providedIn: 'root',
  })
export class EditorService {

  loaderServices: Map<string, IConfigLoaderService> = new Map();

  constructor(
    private http: HttpClient,
    private config: AppConfigService) {}

  public getUser(): Observable<string> {
    return this.http.get<EditorResult<UserName>>(`${this.config.serviceRoot}user`)
      .map(result => new StripSuffixPipe().transform(result.attributes.user_name, '@UBERIT.NET'));
  }

  public getServiceNames(): Observable<string[]> {
      return this.http.get<EditorResult<any>>(`${this.config.serviceRoot}user`)
        .map(result => result.attributes.services)
        .map(arr => arr.map(serviceName => serviceName.name));
  }

  public getSensorFields(): Observable<SensorFields[]> {
    return this.http.get<EditorResult<SensorFieldTemplate>>(
        `${this.config.serviceRoot}api/v1/sensorfields`)
        .map(result => result.attributes.sensor_template_fields.sort((a, b) => {
            if (a.sensor_name > b.sensor_name) {
                return 1;
            } else if (a.sensor_name < b.sensor_name) {
                return -1;
            } else {
                return 0;
            }
        }
    ));
  }

  public createLoaders() {
    this.config.getServiceList().forEach(element => {
        this.loaderServices.set(element, new ConfigLoaderService(this.http, this.config, element));
    });
  }

  public getTestCaseSchema(): Observable<any> {
    // TODO cahnge back once augmenting the test schema is implemented in the backend
    return this.http.get<EditorResult<SchemaInfo>>(`http://localhost:4200/assets/testStrategySchema.json`)
        .map(x => x.attributes.rules_schema);
    // return this.http.get<EditorResult<SchemaInfo>>(`${this.config.serviceRoot}api/v1/testcases/schema`)
    //     .map(x => x.attributes.rules_schema);
  }

  public validateTestCase(testcase: TestCase): Observable<EditorResult<ExceptionInfo>> {
    const outObj = {
        files: [{
            content: testcase,
        }],
    }

    return this.http.post<EditorResult<ExceptionInfo>>(`${this.config.serviceRoot}api/v1/testcases/validate`, outObj);
  }

  public evaluateTestCase(testcase: TestCase, testResult: any): Observable<any> {
    const outObj: TestCaseEvaluation = {
        files: [{
            content: testcase,
        }],
        test_result_raw_output: JSON.stringify(testResult.parsedMessages[0], (key, value) => {if (value !== null) {return value}}, 2),
    }
    const headers = new HttpHeaders();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    return this.http.post<EditorResult<TestCaseResult>>(`${this.config.serviceRoot}api/v1/testcases/evaluate`, outObj)
        .map(x => x.attributes);
  }

  public getLoader(serviceName: string): IConfigLoaderService {
    try {
        return this.loaderServices.get(serviceName);
    } catch {
        throw new DOMException('Invalid service name - can\'t do nothing');
    }
  }
}
