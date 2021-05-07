import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config.service';
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
  TestSchemaInfo,
  AdminSchemaInfo,
  AdminConfig,
  AdminConfigGitFiles,
  DeploymentGitFiles,
} from '@model/config-model';
import { TestCase, TestCaseMap, TestCaseResult, TestCaseWrapper } from '@model/test-case';
import { ADMIN_VERSION_FIELD_NAME, UiMetadata } from '@model/ui-metadata-map';

import { cloneDeep } from 'lodash';
import { map, mergeMap } from 'rxjs/operators';
import { JSONSchema7 } from 'json-schema';
import { TestCaseEvaluation, TestCaseResultAttributes } from '../model/config-model';
import { TestCaseEvaluationResult, isNewTestCase } from '../model/test-case';
import { replacer } from '@app/commons/helper-functions';

export class ConfigLoaderService {
  // eslint-disable-next-line @typescript-eslint/ban-types
  private labelsFunc: Function;

  constructor(
    private http: HttpClient,
    private config: AppConfigService,
    private serviceName: string,
    private uiMetadata: UiMetadata
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
      author: file.content[this.uiMetadata.author],
      configData: file.content,
      description: file.content[this.uiMetadata.description],
      fileHistory: file.file_history,
      isDeployed: false,
      isNew: false,
      name: file.content[this.uiMetadata.name],
      savedInBackend: true,
      tags: this.labelsFunc(file.content),
      testCases: [],
      version: file.content[this.uiMetadata.version],
      versionFlag: -1,
    };
  }

  public getConfigs(): Observable<Config[]> {
    return this.http
      .get<GitFiles<any>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`)
      .map(result => {
        if (result.files) {
          return result.files.map(file => this.getConfigFromFile(file));
        }

        throw new DOMException('bad format response when loading configs');
      });
  }

  public getTestSpecificationSchema(): Observable<JSONSchema7> {
    return this.http
      .get<TestSchemaInfo>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/testschema`)
      .pipe(map(x => x.test_schema));
  }

  public getSchema(): Observable<JSONSchema7> {
    return this.http.get<SchemaInfo>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/schema`).map(x => {
      try {
        return x.rules_schema;
      } catch {
        throw new Error("Call to schema endpoint didn't return the expected schema");
      }
    });
  }

  public getAdminSchema(): Observable<JSONSchema7> {
    return this.http
      .get<AdminSchemaInfo>(`${this.config.serviceRoot}api/v1/${this.serviceName}/adminconfig/schema`)
      .map(x => {
        try {
          return x.admin_config_schema;
        } catch {
          throw new Error("Call to schema endpoint didn't return the expected schema");
        }
      });
  }

  public getPullRequestStatus(): Observable<PullRequestInfo> {
    return this.http.get<PullRequestInfo>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release/status`
    );
  }

  public getAdminPullRequestStatus(): Observable<PullRequestInfo> {
    return this.http.get<PullRequestInfo>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/adminconfig/status`
    );
  }

  public getRelease(): Observable<DeploymentWrapper> {
    return this.http
      .get<DeploymentGitFiles<any>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release`)
      .pipe(
        map(result => {
          const file = result.files[0];
          if (file) {
            let extras = {};
            if (this.uiMetadata.deployment.extras) {
              extras = this.uiMetadata.deployment.extras.reduce((a, x) => ({ ...a, [x]: file.content[x] }), {});
            }
            return {
              deploymentHistory: file.file_history,
              storedDeployment: {
                ...extras,
                ...{
                  configs: file.content[this.uiMetadata.deployment.config_array].map(configData => ({
                    author: configData[this.uiMetadata.author],
                    configData,
                    description: configData[this.uiMetadata.description],
                    isNew: false,
                    name: configData[this.uiMetadata.name],
                    savedInBackend: true,
                    tags: this.labelsFunc(configData),
                    version: configData[this.uiMetadata.version],
                    versionFlag: -1,
                  })),
                  deploymentVersion: file.content[this.uiMetadata.deployment.version],
                },
              },
            };
          }
          if (result.rules_version !== 0) {
            throw new Error('Unexpected files from backend');
          }
          return {
            deploymentHistory: [],
            storedDeployment: {
              configs: [],
              deploymentVersion: 0,
            },
          };
        })
      );
  }

  public getAdminConfig(): Observable<AdminConfig> {
    return this.http
      .get<AdminConfigGitFiles<any>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/adminconfig`)
      .pipe(
        map(result => {
          const file = result.files[0];
          if (file) {
            return {
              configData: file.content,
              fileHistory: file.file_history,
              version: file.content[ADMIN_VERSION_FIELD_NAME],
            };
          }
          if (result.config_version !== 0) {
            throw new Error('Unexpected files from backend');
          }
          return {
            configData: undefined,
            fileHistory: [],
            version: 0,
          };
        })
      );
  }

  public getTestCases(): Observable<TestCaseMap> {
    return this.http
      .get<GitFiles<Content<any>>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`)
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  public validateConfig(config: Config): Observable<any> {
    const json = JSON.stringify(config.configData, null, 2);

    return this.validateConfigJson(json);
  }

  public validateConfigJson(json: string): Observable<any> {
    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate?singleConfig=true`,
      json
    );
  }

  public validateRelease(deployment: Deployment): Observable<any> {
    const validationFormat = this.marshalDeploymentFormat(deployment);
    const json = JSON.stringify(validationFormat, null, 2);

    return this.http.post<any>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate`, json);
  }

  public validateAdminConfig(config: AdminConfig): Observable<any> {
    const json = JSON.stringify(config.configData, null, 2);

    return this.http.post<any>(`${this.config.serviceRoot}api/v1/${this.serviceName}/adminconfig/validate`, json);
  }

  public submitRelease(deployment: Deployment): Observable<any> {
    const releaseFormat = this.marshalDeploymentFormat(deployment);
    const json = JSON.stringify(releaseFormat, null, 2);

    return this.http.post<any>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release`, json);
  }

  public submitConfig(config: Config): Observable<Config[]> {
    const fun = config.isNew ? this.submitNewConfig(config) : this.submitConfigEdit(config);
    return fun.map(result => {
      if (result.files && result.files.length > 0) {
        return result.files.map(file => this.getConfigFromFile(file));
      }

      throw new DOMException('bad format response when submiting a config');
    });
  }

  public submitConfigEdit(config: Config): Observable<GitFiles<any>> {
    const json = JSON.stringify(config.configData, null, 2);

    return this.http.put<GitFiles<any>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`,
      json
    );
  }

  public submitNewConfig(config: Config): Observable<GitFiles<any>> {
    const json = JSON.stringify(config.configData, null, 2);

    return this.http.post<GitFiles<any>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`,
      json
    );
  }

  public submitAdminConfig(config: AdminConfig): Observable<GitFiles<any>> {
    const json = JSON.stringify(config.configData, null, 2);

    return this.http.post<GitFiles<any>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/adminconfig`,
      json
    );
  }

  public testDeploymentConfig(deployment: Deployment, testSpecification: any): Observable<ConfigTestResult> {
    const testDto: ConfigTestDto = {
      files: [
        {
          content: deployment,
        },
      ],
      test_specification: testSpecification,
    };

    testDto.files[0].content = this.marshalDeploymentFormat(testDto.files[0].content);

    return this.http.post<ConfigTestResult>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/test?singleConfig=false`,
      testDto
    );
  }

  public testSingleConfig(configData: any, testSpecification: any): Observable<ConfigTestResult> {
    const testDto: ConfigTestDto = {
      files: [
        {
          content: configData,
        },
      ],
      test_specification: testSpecification,
    };

    return this.http.post<any>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/test?singleConfig=true`,
      testDto
    );
  }

  public submitTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap> {
    return isNewTestCase(testCase) ? this.submitNewTestCase(testCase) : this.submitTestCaseEdit(testCase);
  }

  public submitTestCaseEdit(testCase: TestCaseWrapper): Observable<TestCaseMap> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http
      .put<GitFiles<TestCase>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`, json)
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  public submitNewTestCase(testCase: TestCaseWrapper): Observable<TestCaseMap> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http
      .post<GitFiles<TestCase>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`, json)
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  public validateTestCase(testcase: TestCase): Observable<any> {
    const outObj = {
      files: [
        {
          content: testcase,
        },
      ],
    };
    const json = JSON.parse(JSON.stringify(outObj, replacer, 2));

    return this.http.post<any>(`${this.config.serviceRoot}api/v1/testcases/validate`, json);
  }

  public evaluateTestCase(configData: any, testCaseWrapper: TestCaseWrapper): Observable<TestCaseResult> {
    const ret = {} as TestCaseResult;

    return this.testSingleConfig(configData, testCaseWrapper.testCase.test_specification)
      .map((result: ConfigTestResult) => {
        ret.testResult = result;
        return result;
      })
      .pipe(
        mergeMap(testResult =>
          this.evaluateTestCaseFromResult(testCaseWrapper.testCase, testResult.test_result_raw_output)
        ),
        map((evaluationResult: TestCaseEvaluationResult) => {
          ret.evaluationResult = evaluationResult;
          ret.isRunning = false;
          return ret;
        })
      );
  }

  public evaluateTestCaseFromResult(testcase: TestCase, testResult: any): Observable<TestCaseEvaluationResult> {
    const outObj: TestCaseEvaluation = {
      files: [
        {
          content: testcase,
        },
      ],
      test_result_raw_output: JSON.stringify(testResult, replacer, 2),
    };
    const headers = new HttpHeaders();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    return this.http
      .post<TestCaseResultAttributes>(`${this.config.serviceRoot}api/v1/testcases/evaluate`, outObj)
      .pipe(map(x => x.test_case_result));
  }

  private testCaseFilesToMap(result: GitFiles<any>): TestCaseMap {
    const testCaseMap: TestCaseMap = {};
    if (result.files && result.files.length > 0) {
      result.files.forEach(file => {
        if (!testCaseMap.hasOwnProperty(file.content.config_name)) {
          testCaseMap[file.content.config_name] = [];
        }

        const testCaseWrapper: TestCaseWrapper = {
          fileHistory: file.file_history,
          testCase: file.content,
          testCaseResult: null,
        };

        testCaseMap[file.content.config_name].push(testCaseWrapper);
      });
    }

    return testCaseMap;
  }

  private marshalDeploymentFormat(deployment: Deployment): any {
    const d = cloneDeep(deployment);
    delete d.deploymentVersion;
    delete d.configs;

    return Object.assign(d, {
      [this.uiMetadata.deployment.version]: deployment.deploymentVersion,
      [this.uiMetadata.deployment.config_array]: deployment.configs.map(config => cloneDeep(config.configData)),
    });
  }
}
