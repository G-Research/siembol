import { ConfigTestDto, DeploymentWrapper } from './model/config-model';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppConfigService } from '@app/config';
import { ConfigLoaderService } from '@app/config-loader.service';
import {
  ConfigData,
  ConfigWrapper,
  Deployment,
  EditorResult,
  ExceptionInfo,
  GitFiles,
  PullRequestInfo,
  RepositoryLinks,
  SchemaDto,
  SensorFieldTemplate,
  UserName
} from '@app/model';
import { Field, SensorFields } from '@app/model/sensor-fields';
import { StripSuffixPipe } from '@app/pipes';
import { Observable } from 'rxjs';

export interface IConfigLoaderService {
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
};

@Injectable({
    providedIn: 'root',
  })
export class EditorService {

  loaderServices: Map<string, ConfigLoaderService> = new Map();

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

  public getLoader(serviceName: string): any {
    try {
        return this.loaderServices.get(serviceName);
    } catch {
        throw new DOMException('Invalid service name - can\'t do nothing');
    }
  }
}
