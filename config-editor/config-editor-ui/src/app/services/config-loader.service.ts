import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from '../config/app-config.service';
import {
  ConfigTestDto,
  ConfigTestResult,
  Config,
  Content,
  Deployment,
  DeploymentWrapper,
  GitFiles,
  PullRequestInfo,
  SchemaInfo,
  TestSchemaInfo
} from '@model/config-model';
import {
  TestCase,
  TestCaseMap,
  TestCaseResult,
  TestCaseWrapper,
} from '@model/test-case';
import { UiMetadataMap } from '@model/ui-metadata-map';

import { cloneDeep } from 'lodash';
import { map, mergeMap } from 'rxjs/operators';
import { JSONSchema7 } from 'json-schema';
import { TestCaseEvaluation, TestCaseResultAttributes } from '../model/config-model';
import { TestCaseEvaluationResult, isNewTestCase } from '../model/test-case';

export class ConfigLoaderService {
  private labelsFunc: Function;

  constructor(
    private http: HttpClient,
    private config: AppConfigService,
    private serviceName: string,
    private uiMetadata: UiMetadataMap
  ) {
    try {
      this.labelsFunc = new Function('model', this.uiMetadata.labelsFunc);
    } catch {
      console.error('unable to parse labels function');
      this.labelsFunc = () => [];
    }
  }

  public getConfigFromFile(file: any): Config {
    return {
      isNew: false,
      configData: file.content,
      savedInBackend: true,
      name: file.content[this.uiMetadata.name],
      description: file.content[this.uiMetadata.description],
      author: file.content[this.uiMetadata.author],
      version: file.content[this.uiMetadata.version],
      versionFlag: -1,
      isDeployed: false,
      tags: this.labelsFunc(file.content),
      fileHistory: file.file_history,
      testCases: []
    }
  } 

  public getConfigs(): Observable<Config[]> {
    return this.http
      .get<GitFiles<any>>(
        `${this.config.serviceRoot}api/v1/${
        this.serviceName
        }/configstore/configs`
      )
      .map(result => {
        if (
          result.files
        ) {
          return result.files.map(file => this.getConfigFromFile(file));
        }

        throw new DOMException('bad format response when loading configs');
      });
  }

  public getConfigsFromFiles(
    files: Content<any>[]
  ): Config[] {
    return files.map(file => this.getConfigFromFile(file));
  }

  public getTestSpecificationSchema(): Observable<JSONSchema7> {
    return this.http
      .get<TestSchemaInfo>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configs/testschema`
      )
      .pipe(map(x => x.test_schema));
  }

  public getSchema(): Observable<JSONSchema7> {
    return this.http
      .get<SchemaInfo>(
        `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/schema`
      )
      .map(x => {
        try {
          return x.rules_schema;
        } catch {
          throw new Error(
            "Call to schema endpoint didn't return the expected schema"
          );
        }
      });
  }

  public getPullRequestStatus(): Observable<PullRequestInfo> {
    return this.http
      .get<PullRequestInfo>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/release/status`
      );
  }

  public getRelease(): Observable<DeploymentWrapper> {
    return this.http
      .get<GitFiles<any>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/release`
      )
      .pipe(
        map(result => {
          return result.files[0]
        }),
        map(result => {
          let extras = {};
          if (this.uiMetadata.deployment.extras) {
            extras = this.uiMetadata.deployment.extras
              .reduce((a, x) => (
                { ...a, [x]: result.content[x] }), {}
              )
          }
          return ({
          deploymentHistory: result.file_history,
          storedDeployment: {
            ... extras,
            ... {
              deploymentVersion:
                result.content[this.uiMetadata.deployment.version],
              configs: result.content[
                this.uiMetadata.deployment.config_array
              ].map(configData => ({
                isNew: false,
                configData: configData,
                savedInBackend: true,
                name: configData[this.uiMetadata.name],
                description: configData[this.uiMetadata.description],
                author: configData[this.uiMetadata.author],
                version: configData[this.uiMetadata.version],
                versionFlag: -1,
                tags: this.labelsFunc(configData)
              }))
            }
          }
        })})
      );
  }

  public getTestCases(): Observable<TestCaseMap> {
    return this.http
      .get<GitFiles<Content<any>>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/testcases`
      )
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  private testCaseFilesToMap(
    result: GitFiles<any>
  ): TestCaseMap {
    const testCaseMap: TestCaseMap = {};
    if (
      result.files &&
      result.files.length > 0
    ) {
      result.files.forEach(file => {
        if (!testCaseMap.hasOwnProperty(file.content.config_name)) {
          testCaseMap[file.content.config_name] = [];
        }

        const testCaseWrapper: TestCaseWrapper = {
          testCase: file.content,
          testCaseResult: null,
          fileHistory: file.file_history
        };

        testCaseMap[file.content.config_name].push(testCaseWrapper);
      });
    }

    return testCaseMap;
  }

  public validateConfig(
    config: Config
  ): Observable<any> {
    const json = JSON.stringify(
      config.configData,
      null,
      2
    );

    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configs/validate?singleConfig=true`,
      json
    );
  }

  public validateRelease(
    deployment: Deployment
  ): Observable<any> {
    const validationFormat = this.marshalDeploymentFormat(
      deployment
    );
    const json = JSON.stringify(validationFormat, null, 2);

    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate`,
      json
    );
  }

  public submitRelease(
    deployment: Deployment
  ): Observable<any> {
    const releaseFormat = this.marshalDeploymentFormat(
      deployment
    );
    const json = JSON.stringify(releaseFormat, null, 2);

    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/release`,
      json
    );
  }

  public submitConfig(config: Config): Observable<Config[]> {
    const fun = config.isNew ? this.submitNewConfig(config) : this.submitConfigEdit(config);
    return fun.map(result => {
      if (
        result.files &&
        result.files.length > 0
      ) {
        return result.files.map(file => this.getConfigFromFile(file));
      }

      throw new DOMException('bad format response when submiting a config');
    });
  }


  public submitConfigEdit(
    config: Config
  ): Observable<GitFiles<any>> {
    const json = JSON.stringify(
      config.configData,
      null,
      2
    );

    return this.http.put<GitFiles<any>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/configs`,
      json
    );
  }

  public submitNewConfig(
    config: Config
  ): Observable<GitFiles<any>> {
    const json = JSON.stringify(
      config.configData,
      null,
      2
    );

    return this.http.post<GitFiles<any>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/configs`,
      json
    );
  }
  
  public testDeploymentConfig(
    testDto: ConfigTestDto
  ): Observable<ConfigTestResult> {
    testDto.files[0].content = this.marshalDeploymentFormat(
      testDto.files[0].content
    );

    return this.http.post<ConfigTestResult>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configs/test?singleConfig=false`,
      testDto
    );
  }

  public testSingleConfig(configData: any, testSpecification: any): Observable<ConfigTestResult> {
    const testDto: ConfigTestDto = {
      files: [
        {
          content: configData
        }
      ],
      test_specification: testSpecification
    };

  
    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configs/test?singleConfig=true`,
      testDto
    );
  }


  public submitTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap> {
    return isNewTestCase(testCase) 
    ? this.submitNewTestCase(testCase) : this.submitTestCaseEdit(testCase);
  }

  public submitTestCaseEdit(
    testCase: TestCaseWrapper
  ): Observable<TestCaseMap> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http
      .put<GitFiles<TestCase>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/testcases`,
        json
      )
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  public submitNewTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http
      .post<GitFiles<TestCase>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/testcases`,
        json
    )
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  private replacer(key, value) {
    return value === null ? undefined : value;
  }


  public validateTestCase(testcase: TestCase): Observable<any> {
    const outObj = {
      files: [{
        content: testcase,
      }],
    }
    const json = JSON.parse(JSON.stringify(outObj, this.replacer, 2));

    return this.http.
      post<any>(
        `${this.config.serviceRoot}api/v1/testcases/validate`, json
      );
  }

  public evaluateTestCase(configData: any, testCaseWrapper: TestCaseWrapper): Observable<TestCaseResult> {
    let ret = {} as TestCaseResult;

    return this.testSingleConfig(configData, testCaseWrapper.testCase.test_specification)
      .map((result: ConfigTestResult) => {
        ret.testResult = result;
        return result;
      }).pipe(
        mergeMap(testResult => this.evaluateTestCaseFromResult(testCaseWrapper.testCase, testResult.test_result_raw_output)),
        map((evaluationResult: TestCaseEvaluationResult) => {
          ret.evaluationResult = evaluationResult;
          ret.isRunning = false;
          return ret;
        }));
  }

  public evaluateTestCaseFromResult(testcase: TestCase, testResult: any): Observable<TestCaseEvaluationResult> {
    const outObj: TestCaseEvaluation = {
      files: [{
        content: testcase,
      }],
      test_result_raw_output: JSON.stringify(testResult, this.replacer, 2),
    }
    const headers = new HttpHeaders();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    return this.http.post<TestCaseResultAttributes>(`${this.config.serviceRoot}api/v1/testcases/evaluate`,
      outObj).pipe(map(x => x.test_case_result)
      )
  }

  private marshalDeploymentFormat(deployment: Deployment): any {
    const d = cloneDeep(deployment);
    delete d.deploymentVersion;
    delete d.configs;

    return Object.assign(d, {
        [this.uiMetadata.deployment.version]: deployment.deploymentVersion,
        [this.uiMetadata.deployment.config_array]:
            deployment.configs.map(config => cloneDeep(config.configData)),
    });
}
}
