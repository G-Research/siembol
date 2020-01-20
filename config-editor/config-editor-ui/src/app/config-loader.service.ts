import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from './config/app-config.service';
import { IConfigLoaderService } from './editor.service';
import { SchemaDto } from './model';
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
    RepositoryLinks,
    RepositoryLinksWrapper,
    SchemaInfo,
    TestSchemaInfo,
} from './model/config-model';
import { Field } from './model/sensor-fields';
import { TestCaseMap, TestCaseResultDefault, TestCaseWrapper, TestState } from './model/test-case';
import { UiMetadataMap } from './model/ui-metadata-map';

import { cloneDeep } from 'lodash';
import { map, tap } from 'rxjs/operators';

export class ConfigLoaderService implements IConfigLoaderService {
    private optionalObjects: string[] = [];
    private readonly uiMetadata: UiMetadataMap;
    private labelsFunc: Function;
    public originalSchema;
    public modelOrder = {};

  constructor(private http: HttpClient, private config: AppConfigService, private serviceName: string) {
    this.uiMetadata = this.config.getUiMetadata(this.serviceName);
    try {
        this.labelsFunc = new Function('model', this.uiMetadata.labelsFunc);
    } catch {
        console.error('unable to parse labels function');
        this.labelsFunc = () => [];
    }
  }

  public getConfigs(): Observable<ConfigWrapper<ConfigData>[]> {

    return this.http.get<EditorResult<GitFiles<any>>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`)
      .map(result => {
        if (result.attributes && result.attributes.files && result.attributes.files.length > 0) {
          return result.attributes.files.map(file => ({
              isNew: false,
              configData: this.wrapOptionalsInArray(file.content),
              savedInBackend: true,
              name: file.content[this.uiMetadata.name],
              description: file.content[this.uiMetadata.description],
              author: file.content[this.uiMetadata.author],
              version: file.content[this.uiMetadata.version],
              versionFlag: -1,
              isDeployed: false,
              tags: this.labelsFunc(file.content),
              fileHistory: file.file_history,
          }));
        }

        throw new DOMException('bad format response when loading configs');
      });
  }

  public getConfigsFromFiles(files: Content<any>[]): ConfigWrapper<ConfigData>[] {
    const ret: ConfigWrapper<ConfigData>[] = [];
    for (const file of files) {
        ret.push({
            isNew: false,
            configData: this.wrapOptionalsInArray(file.content),
            savedInBackend: true,
            name: file.content[this.uiMetadata.name],
            description: file.content[this.uiMetadata.description],
            author: file.content[this.uiMetadata.author],
            version: file.content[this.uiMetadata.version],
            versionFlag: -1,
            isDeployed: false,
            tags: this.labelsFunc(file.content),
        });
    }

    return ret;
  }

  private returnSubTree(tree, path: string): any {
    let subtree = cloneDeep(tree);
    path.split('.').forEach(node => {
        subtree = subtree[node];
    });

    return subtree;
  }

  public getTestSpecificationSchema(): Observable<any> {
    return this.http.get<EditorResult<TestSchemaInfo>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/testschema`)
      .map(x =>
          x.attributes.test_schema
    );
  }

  public getSchema(): Observable<SchemaDto> {
    return this.http.get<EditorResult<SchemaInfo>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/schema`)
      .map(x => {
          this.originalSchema = x.attributes.rules_schema;
        try {
            return this.returnSubTree(x, this.uiMetadata.perConfigSchemaPath);
        } catch {
            throw new Error('Call to schema endpoint didn\'t return the expected schema');
        }
      })
      .map(schema => {
        this.optionalObjects = []; // clear optional objects in case they have been set previously;
        this.modelOrder = {}
        this.wrapOptionalsInSchema(schema, '', '');
        delete schema.properties[this.uiMetadata.name];
        delete schema.properties[this.uiMetadata.author];
        delete schema.properties[this.uiMetadata.version];
        schema.required = schema.required.filter(
          f => (f !== this.uiMetadata.name) && (f !== this.uiMetadata.author) && (f !== this.uiMetadata.version));

        return { schema };
      });
  }

  // function to go through the output json and reorder the properties such that it is consistent with the schema
  public produceOrderedJson(configData: ConfigData, path: string) {
    if (this.modelOrder[path]) {
        const currentCfg = cloneDeep(configData)
        configData = {};
        for (const key of this.modelOrder[path]) {
            configData[key] = currentCfg[key];
            const searchPath =  path === '/' ? path + key : path + '/' + key;
            // ensure it has children
            if (typeof(configData[key]) === typeof({}) && this.modelOrder[searchPath] !== undefined) {
                if (configData[key].length === undefined) {
                    // is an object
                    const tempCopy = cloneDeep(configData[key])
                    configData[key] = {};
                    const tmpObj = {}
                    for (const orderedKey of this.modelOrder[searchPath]) {
                        if (tempCopy[orderedKey] !== undefined) {
                            tmpObj[orderedKey] = tempCopy[orderedKey];
                        }
                    }
                    configData[key] = tmpObj;
                    configData[key] = this.produceOrderedJson(configData[key], searchPath)
                } else {
                    // is an array
                    const tmp = cloneDeep(configData[key]);
                    configData[key] = [];
                    for (let i = 0; i < tmp.length; ++i) {
                        configData[key].push(this.produceOrderedJson(tmp[i], searchPath));
                    }
                }
            }
        }
    }

    return configData;
  }

  private wrapOptionalsInArray(obj: object) {
      for (const optional of this.optionalObjects) {
        this.findAndWrap(obj, optional);
    }

    return obj;
}
  private findAndWrap(obj: any, optionalKey: string) {
    if (typeof(obj) === typeof ({})) {
      for (const key of Object.keys(obj)) {
        if (key === optionalKey) {
          obj[key] = [obj[key]];

          return;
        }
        this.findAndWrap(obj[key], optionalKey);
      }
    }
  }

  private wrapOptionalsInSchema(obj: any, propKey?: string, path?: string): any {
    if (obj === undefined || obj === null || typeof (obj) !== typeof ({})) {
      return;
    }
    if (obj.type === 'object' && typeof(obj.properties) === typeof ({})) {
        path = path.endsWith('/') ? path + propKey : path + '/' + propKey;
        const requiredProperties = obj.required || [];
        const props = Object.keys(obj.properties);
        this.modelOrder[path] = props;
        for (const property of props) {
            const thingy = obj.properties[property];
            const isRequired = requiredProperties.includes(property);
            const isObject = thingy.type === 'object';
            if (!isRequired && isObject) {
                this.optionalObjects.push(property);
                if (thingy.default) {
                    delete thingy.default;
                }
                const sub = {...thingy};
                thingy.type = 'array';
                delete thingy.required;
                delete thingy.properties;
                delete thingy.title;
                delete thingy.description;

                // tabs is not compatible with the array type so delete it if it is at the parent level but keep it on the sub level
                if (sub['x-schema-form'] !== undefined && sub['x-schema-form']['type'] !== 'tabs') {
                    delete sub['x-schema-form'];
                }
                if (thingy['x-schema-form'] !== undefined && thingy['x-schema-form']['type'] === 'tabs' && thingy['type'] === 'array') {
                    delete thingy['x-schema-form'];
                }
                // ***********************

                thingy.items = sub;
                thingy.maxItems = 1;
                this.wrapOptionalsInSchema(thingy.items, property, path);
            } else {
                this.wrapOptionalsInSchema(thingy, property, path);
            }
        }
    } else if (obj.type === 'array') {
        path = path === '/' ? path : path + '/';
        if (obj.items.type === 'object') {
            this.wrapOptionalsInSchema(obj.items, propKey, path);
        }
    } else if (obj.type === undefined && !obj.hasOwnProperty('properties')) {
        path = path === '/' ? path + propKey : path + '/' + propKey;
        for (const key of Object.keys(obj)) {
            this.wrapOptionalsInSchema(obj[key], key, path);
        }
    }
  }

  public unwrapOptionalsFromArrays(obj: any) {
    if (obj === undefined || obj === null || typeof (obj) !== typeof ({})) {
      return obj;
    }

    for (const key of Object.keys(obj)) {
      if (this.optionalObjects.includes(key)) {
          obj[key] = obj[key] === [] || obj[key] === undefined || obj[key] === null ? undefined : obj[key][0];
      }
    }
    for (const key of Object.keys(obj)) {
    this.unwrapOptionalsFromArrays(obj[key]);
    }

    return obj;
  }

  public getPullRequestStatus(): Observable<PullRequestInfo> {
    return this.http.get<EditorResult<PullRequestInfo>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release/status`)
      .pipe(
          map(result => result.attributes)
      )
  }

    public getRelease(): Observable<DeploymentWrapper> {
        return this.http.get<EditorResult<GitFiles<any>>>
            (`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release`)
            .map(result => result.attributes.files[0])
            .map(result => (
                {
                    deploymentHistory: result.file_history,
                    storedDeployment: {
                        deploymentVersion: result.content[this.uiMetadata.deployment.version],
                        configs: result.content[this.uiMetadata.deployment.config_array].map(configData => ({
                            isNew: false,
                            configData: this.wrapOptionalsInArray(configData),
                            savedInBackend: true,
                            name: configData[this.uiMetadata.name],
                            description: configData[this.uiMetadata.description],
                            author: configData[this.uiMetadata.author],
                            version: configData[this.uiMetadata.version],
                            versionFlag: -1,
                            tags: this.labelsFunc(configData),
                        })),
                    },
                }
            ))
    }

  public getRepositoryLinks(): Observable<RepositoryLinks> {
    return this.http.get<EditorResult<RepositoryLinksWrapper>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/repositories`)
      .map(result => ({
        ...result.attributes.rules_repositories,
        rulesetName: this.serviceName,
      }))
  }

  public getTestCases(): Observable<TestCaseMap> {
    const testCaseMap: TestCaseMap = {};

    return this.http.get<EditorResult<any>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`)
        .map(result => {
            if (result.attributes && result.attributes.files && result.attributes.files.length > 0) {
                result.attributes.files.forEach(file => {
                    if (!testCaseMap.hasOwnProperty(file.content.config_name)) {
                        testCaseMap[file.content.config_name] = [];
                    }
                    const testCase: TestCaseWrapper = {
                        testCase: file.content,
                        testState: TestState.NOT_RUN,
                        testResult: new TestCaseResultDefault(),
                        fileHistory: file.file_history,
                    }
                    testCaseMap[file.content.config_name].push(testCase);
                });
            }

            return testCaseMap;
        });

  }

  public validateConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<ExceptionInfo>> {
    const json = JSON.stringify(this.unwrapOptionalsFromArrays(cloneDeep(config.configData)), null, 2);

    return this.http.post<EditorResult<ExceptionInfo>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate?singleConfig=true`, json);
  }

  public validateRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>> {
    const validationFormat = this.marshalDeploymentFormat(deployment);
    const json = JSON.stringify(validationFormat, null, 2);

    return this.http.post<EditorResult<ExceptionInfo>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configs/validate`, json);
  }

  public submitRelease(deployment: Deployment<ConfigWrapper<ConfigData>>): Observable<EditorResult<ExceptionInfo>> {
    const releaseFormat = this.marshalDeploymentFormat(deployment);
    const json = JSON.stringify(releaseFormat, null, 2);

    return this.http.post<EditorResult<ExceptionInfo>>(`${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/release`, json);
  }

  public submitConfigEdit(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>> {
    const json = JSON.stringify(this.unwrapOptionalsFromArrays(cloneDeep(config.configData)), null, 2);

    return this.http.put<EditorResult<GitFiles<ConfigData>>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`, json);
  }

  public submitNewConfig(config: ConfigWrapper<ConfigData>): Observable<EditorResult<GitFiles<ConfigData>>> {
    const json = JSON.stringify(this.unwrapOptionalsFromArrays(cloneDeep(config.configData)), null, 2);

    return this.http.post<EditorResult<GitFiles<ConfigData>>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/configs`, json);
  }

  public getFields(): Observable<Field[]> {
      return this.http.get<EditorResult<any>>(
          `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/fields`)
          .map(f => f.attributes.fields);
  }

  public testDeploymentConfig(testDto: ConfigTestDto): Observable<EditorResult<ConfigTestResult>> {
    testDto.files[0].content = this.marshalDeploymentFormat(testDto.files[0].content);

    return this.http.post<EditorResult<any>>(
        `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/test?singleConfig=false`, testDto)
  }

  public testSingleConfig(testDto: ConfigTestDto): Observable<EditorResult<ConfigTestResult>> {
    testDto.files[0].content = this.unwrapOptionalsFromArrays(cloneDeep(testDto.files[0].content));

    return this.http.post<EditorResult<any>>(
        `${this.config.serviceRoot}api/v1/${this.serviceName}/configs/test?singleConfig=true`, testDto)
  }

  public submitTestCaseEdit(testCase: TestCaseWrapper): Observable<EditorResult<GitFiles<ConfigData>>> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http.put<EditorResult<GitFiles<ConfigData>>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`, json);
  }

  public submitNewTestCase(testCase: TestCaseWrapper): Observable<EditorResult<GitFiles<ConfigData>>> {
    const json = JSON.stringify(cloneDeep(testCase.testCase), null, 2);

    return this.http.post<EditorResult<GitFiles<ConfigData>>>(
      `${this.config.serviceRoot}api/v1/${this.serviceName}/configstore/testcases`, json);
  }

  public marshalDeploymentFormat(deployment: Deployment<ConfigWrapper<ConfigData>>): any {
    const d = cloneDeep(deployment);
    delete d.deploymentVersion;
    delete d.configs;

    return Object.assign(d, {
      [this.uiMetadata.deployment.version]: deployment.deploymentVersion,
      [this.uiMetadata.deployment.config_array]:
        deployment.configs.map(config => this.unwrapOptionalsFromArrays(cloneDeep(config.configData))),
    });
  }
}
