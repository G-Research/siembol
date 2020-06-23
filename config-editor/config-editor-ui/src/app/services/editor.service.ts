import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppConfigService } from '../config';
import { StripSuffixPipe } from '../pipes';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigWrapperService } from './config-wrapper-service';
import { ConfigData, ConfigWrapper, Deployment, GitFiles, PullRequestInfo, RepositoryLinks, SensorFields,
    SensorFieldTemplate, UserName, RepositoryLinksWrapper } from '@model';
import { ConfigTestDto, DeploymentWrapper, EditorResult, ExceptionInfo, SchemaInfo, TestCaseEvaluation } from '@model/config-model';
import { TestCase, TestCaseMap, TestCaseResult, TestCaseWrapper } from '@model/test-case';
import { Field } from '@model/sensor-fields';
import { JSONSchema7 } from 'json-schema';

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
  getFields(): Observable<Field[]>
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
    return this.http.get<EditorResult<UserName>>(`${this.config.serviceRoot}user`).pipe(
        map(result => new StripSuffixPipe().transform(result.attributes.user_name, '@UBERIT.NET'))
    );
  }

  public getServiceNames(): Observable<string[]> {
      return this.http.get<EditorResult<any>>(`${this.config.serviceRoot}user`).pipe(
          map(result => result.attributes.services),
          map(arr => arr.map(serviceName => serviceName.name))
      );
  }

  public getSensorFields(): Observable<SensorFields[]> {
    return this.http.get<EditorResult<SensorFieldTemplate>>(
        `${this.config.serviceRoot}api/v1/sensorfields`).pipe(
            map(result => result.attributes.sensor_template_fields.sort((a, b) => {
                if (a.sensor_name > b.sensor_name) {
                    return 1;
                } else if (a.sensor_name < b.sensor_name) {
                    return -1;
                } else {
                    return 0;
                }
            }
        ))
    )
  }

  public getRepositoryLinks(serviceName): Observable<RepositoryLinks> {
    return this.http.get<EditorResult<RepositoryLinksWrapper>>(
      `${this.config.serviceRoot}api/v1/${serviceName}/configstore/repositories`).pipe(
        map(result => ({
            ...result.attributes.rules_repositories,
            rulesetName: serviceName,
        }))
      )
  }

  public createLoader(serviceName: string) {
      this.configWrapper = new ConfigWrapperService(this.config.getUiMetadata(serviceName));
      this.configLoader = new ConfigLoaderService(this.http, this.config, serviceName, this.configWrapper);
  }

  public getTestCaseSchema(): Observable<any> {
    // TODO cahnge back once augmenting the test schema is implemented in the backend
    return this.http.get<EditorResult<SchemaInfo>>(`${window.location.origin}/assets/testStrategySchema.json`).pipe(
        map(x => x.attributes.rules_schema)
    )
    // return this.http.get<EditorResult<SchemaInfo>>(`${this.config.serviceRoot}api/v1/testcases/schema`)
    //     .map(x => x.attributes.rules_schema);
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
