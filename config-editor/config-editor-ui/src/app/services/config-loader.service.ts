import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { AppConfigService } from '../config/app-config.service';
import { IConfigLoaderService } from './editor.service';
import {
  ConfigData,
  ConfigTestDto,
  ConfigTestResult,
  ConfigWrapper,
  Content,
  Deployment,
  DeploymentWrapper,
  EditorResult,
  ExceptionInfo,
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
import { ConfigWrapperService } from './config-wrapper-service';
import { JSONSchema7 } from 'json-schema';
import { TestCaseEvaluation, TestCaseResultAttributes } from '../model/config-model';
import { StatusCode } from '../commons';
import { TestCaseEvaluationResult, isNewTestCase } from '../model/test-case';

export class ConfigLoaderService implements IConfigLoaderService {
  private optionalObjects: string[] = [];
  private readonly uiMetadata: UiMetadataMap;
  private labelsFunc: Function;
  public originalSchema: JSONSchema7;
  public modelOrder = {};

  constructor(
    private http: HttpClient,
    private config: AppConfigService,
    private serviceName: string,
    private configWrapperService: ConfigWrapperService
  ) {
    this.uiMetadata = this.config.getUiMetadata(this.serviceName);
    try {
      this.labelsFunc = new Function('model', this.uiMetadata.labelsFunc);
    } catch {
      console.error('unable to parse labels function');
      this.labelsFunc = () => [];
    }
  }

  public getConfigWrapperFromFile(file: any): ConfigWrapper<ConfigData> {
    return {
      isNew: false,
      configData: this.configWrapperService.wrapConfig(file.content),
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

  public getConfigs(): Observable<ConfigWrapper<ConfigData>[]> {
    return this.http
      .get<EditorResult<GitFiles<any>>>(
        `${this.config.serviceRoot}api/v1/${
        this.serviceName
        }/configstore/configs`
      )
      .map(result => {
        if (
          result.attributes &&
          result.attributes.files &&
          result.attributes.files.length > 0
        ) {
          return result.attributes.files.map(file => this.getConfigWrapperFromFile(file));
        }

        throw new DOMException('bad format response when loading configs');
      });
  }

  public getConfigsFromFiles(
    files: Content<any>[]
  ): ConfigWrapper<ConfigData>[] {
    return files.map(file => this.getConfigWrapperFromFile(file));
  }

  private returnSubTree(tree, path: string): any {
    let subtree = cloneDeep(tree);
    path.split('.').forEach(node => {
      subtree = subtree[node];
    });

    return subtree;
  }

  public getTestSpecificationSchema(): Observable<JSONSchema7> {
    return this.http
      .get<EditorResult<TestSchemaInfo>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configs/testschema`
      )
      .pipe(map(x => x.attributes.test_schema));
  }

  public getSchema(): Observable<JSONSchema7> {
    return this.http
      .get<EditorResult<SchemaInfo>>(
        `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/schema`
      )
      .map(x => {
        this.originalSchema = x.attributes.rules_schema;
        try {
          return this.returnSubTree(x, this.uiMetadata.perConfigSchemaPath) as JSONSchema7;
        } catch {
          throw new Error(
            "Call to schema endpoint didn't return the expected schema"
          );
        }
      })
      .map(schema => {
        this.optionalObjects = []; // clear optional objects in case they have been set previously;
        this.modelOrder = {};
        this.configWrapperService.wrapOptionalsInSchema(schema, '', '');
        delete schema.properties[this.uiMetadata.name];
        delete schema.properties[this.uiMetadata.author];
        delete schema.properties[this.uiMetadata.version];
        schema.required = schema.required.filter(
          f =>
            f !== this.uiMetadata.name &&
            f !== this.uiMetadata.author &&
            f !== this.uiMetadata.version
        );

        return schema;
      });
  }

  public getPullRequestStatus(): Observable<PullRequestInfo> {
    return this.http
      .get<EditorResult<PullRequestInfo>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/release/status`
      )
      .pipe(map(result => result.attributes));
  }

  public getRelease(): Observable<DeploymentWrapper> {
    return this.http
      .get<EditorResult<GitFiles<any>>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/release`
      )
      .pipe(
        map(result => {
            return result.attributes.files[0]}),
        map(result => ({
          deploymentHistory: result.file_history,
          storedDeployment: {
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
        }))
      );
  }

  public getTestCases(): Observable<TestCaseMap> {
    return this.http
      .get<EditorResult<GitFiles<Content<any>>>>(
        `${this.config.serviceRoot}api/v1/${
          this.serviceName
        }/configstore/testcases`
      )
      .pipe(map(result => this.testCaseFilesToMap(result)));
  }

  private testCaseFilesToMap(
    result: EditorResult<GitFiles<any>>
  ): TestCaseMap {
    const testCaseMap: TestCaseMap = {};
    if (
      result.attributes &&
      result.attributes.files &&
      result.attributes.files.length > 0
    ) {
      result.attributes.files.forEach(file => {
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
    config: ConfigWrapper<ConfigData>
  ): Observable<EditorResult<ExceptionInfo>> {
    const json = JSON.stringify(
      this.configWrapperService.unwrapConfig(config.configData),
      null,
      2
    );

    return this.http.post<EditorResult<ExceptionInfo>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configs/validate?singleConfig=true`,
      json
    );
  }

  public validateRelease(
    deployment: Deployment<ConfigWrapper<ConfigData>>
  ): Observable<EditorResult<ExceptionInfo>> {
    const validationFormat = this.configWrapperService.marshalDeploymentFormat(
      deployment
    );
    const json = JSON.stringify(validationFormat, null, 2);

    return this.http.post<EditorResult<ExceptionInfo>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate`,
      json
    );
  }

  public submitRelease(
    deployment: Deployment<ConfigWrapper<ConfigData>>
  ): Observable<EditorResult<ExceptionInfo>> {
    const releaseFormat = this.configWrapperService.marshalDeploymentFormat(
      deployment
    );
    const json = JSON.stringify(releaseFormat, null, 2);

    return this.http.post<EditorResult<ExceptionInfo>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/release`,
      json
    );
  }

  public submitConfig(config: ConfigWrapper<ConfigData>): Observable<ConfigWrapper<ConfigData>[]> {
    const fun = config.isNew ? this.submitNewConfig(config) : this.submitConfigEdit(config);
    return fun.map(result => {
      if (
        result.attributes &&
        result.attributes.files &&
        result.attributes.files.length > 0
      ) {
        return result.attributes.files.map(file => this.getConfigWrapperFromFile(file));
      }

      throw new DOMException('bad format response when submiting a config');
    });
  }

  public submitConfigEdit(
    config: ConfigWrapper<ConfigData>
  ): Observable<EditorResult<GitFiles<any>>> {
    const json = JSON.stringify(
      this.configWrapperService.unwrapConfig(config.configData),
      null,
      2
    );

    return this.http.put<EditorResult<GitFiles<any>>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/configs`,
      json
    );
  }

  public submitNewConfig(
    config: ConfigWrapper<ConfigData>
  ): Observable<EditorResult<GitFiles<any>>> {
    const json = JSON.stringify(
      this.configWrapperService.unwrapConfig(config.configData),
      null,
      2
    );

    return this.http.post<EditorResult<GitFiles<any>>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configstore/configs`,
      json
    );
  }
  
  public testDeploymentConfig(
    testDto: ConfigTestDto
  ): Observable<EditorResult<ConfigTestResult>> {
    testDto.files[0].content = this.configWrapperService.marshalDeploymentFormat(
      testDto.files[0].content
    );

    return this.http.post<EditorResult<any>>(
      `${this.config.serviceRoot}api/v1/${
        this.serviceName
      }/configs/test?singleConfig=false`,
      testDto
    );
  }

  public testSingleConfig(config: any, testSpecification: any): Observable<EditorResult<ConfigTestResult>> {
    const testDto: ConfigTestDto = {
      files: [
        {
          content: this.configWrapperService.unwrapConfig(config)
        }
      ],
      test_specification: testSpecification
    };

  
    return this.http.post<EditorResult<any>>(
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
      .put<EditorResult<GitFiles<TestCase>>>(
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
      .post<EditorResult<GitFiles<TestCase>>>(
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


  public validateTestCase(testcase: TestCase): Observable<EditorResult<ExceptionInfo>> {
    const outObj = {
        files: [{
            content: testcase,
        }],
    }
    const json = JSON.parse(JSON.stringify(outObj, this.replacer, 2));

    return this.http.post<EditorResult<ExceptionInfo>>(`${this.config.serviceRoot}api/v1/testcases/validate`, json);
  }

  public evaluateTestCase(config: any, testCaseWrapper: TestCaseWrapper): Observable<TestCaseResult> {
    let ret = {} as TestCaseResult;

    return this.testSingleConfig(config, testCaseWrapper.testCase.test_specification)
      .map((result: EditorResult<ConfigTestResult>) => {
        if (!result || result?.status_code !== StatusCode.OK) {
          throwError('problem testing config');
        }
        ret.testResult = result.attributes;
        return result.attributes;
      }).pipe(
        mergeMap(testResult => this.evaluateTestCaseFromResult(testCaseWrapper.testCase, testResult.test_result_raw_output)),
        map((evaluationResult: TestCaseEvaluationResult) => {
          if (!evaluationResult) {
            throwError('problem evaluating test case');
          }
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

    return this.http.post<EditorResult<TestCaseResultAttributes>>(`${this.config.serviceRoot}api/v1/testcases/evaluate`,
      outObj).pipe(map(x => x.attributes.test_case_result)
      )
  }

  public createDeploymentSchema(): JSONSchema7 {
    const depSchema = this.originalSchema;
    depSchema.properties[this.uiMetadata.deployment.config_array] = {};
    delete depSchema.properties[this.uiMetadata.deployment.config_array];
    delete depSchema.properties[this.uiMetadata.deployment.version];
    depSchema.required = depSchema.required.filter(element => {
      if (element !== this.uiMetadata.deployment.version && element !== this.uiMetadata.deployment.config_array) {
        return true;
      }

      return false;
    });

    return depSchema;
  }
}
