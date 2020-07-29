import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { ConfigData, ServiceInfo, EditorResult, UserInfo, SchemaInfo } from '../model/config-model';
import { UiMetadataMap } from '../model/ui-metadata-map';
import { EditorConfig } from './editor-config';
import { BuildInfo } from '@app/model/build-info';
import { StatusCode } from '../commons/status-code';
import { JSONSchema7 } from 'json-schema';

@Injectable({
    providedIn: 'root',
  })
export class AppConfigService {

    private config: EditorConfig;
    private uiMetadata: UiMetadataMap;
    private buildInfo: BuildInfo;
    private user: string;
    private userServices: ServiceInfo[];
    private userServicesMap: Map<string, ServiceInfo>;
    private testCaseSchema: JSONSchema7;

    constructor(private http: HttpClient) { }

    // This gets called on startup and APP_INITIALIZER will wait for the promise to resolve
    public loadConfigAndUserInfo(): Promise<any> {
        return this.loadConfig()
        .then(() => this.loadUiMetadata())
        .then(() => this.loadUserInfo())
        .then(() => this.loadTestCaseSchema());
    }

    private loadConfig(): Promise<any> {
        return this.http.get('config/ui-config.json')
            .toPromise()
            .then((r: ConfigData) => {
                // tslint:disable-next-line:no-console
                console.info(`Loaded ${r.environment} config`, r);
                this.config = r;
            })
    }

    private loadUiMetadata(): Promise<any> {
        return this.http.get('config/ui-bootstrap.json')
            .toPromise()
            .then((r: UiMetadataMap) => {
                // tslint:disable-next-line:no-console
                console.info('loaded UI setup', r);
                this.uiMetadata = r;
            })
    }

    public loadBuildInfo(): Promise<any> {
        return this.http.get('assets/build-info.json')
        .toPromise()
        .then((r: BuildInfo) => {
            console.info('loaded app metadata', r);
            this.buildInfo = r;
        }).catch(err => console.info('could not load build info'));
    }

    private loadUserInfo(): Promise<any> {
        return this.http.get(`${this.config.serviceRoot}user`)
        .toPromise()
        .then((r: EditorResult<UserInfo>) => {
            console.info('loaded user info setup', r);
            if (r === undefined 
                || r.status_code === undefined 
                || r.status_code !== StatusCode.OK 
                || r.attributes.user_name === undefined 
                || r.attributes.services === undefined) {
                    console.error('empty user endpoint response');
                    throw new Error();
            }
            this.user = r.attributes.user_name;
            this.userServices = r.attributes.services;
            this.userServicesMap = new Map(this.userServices.map(x => [x.name, x]));

            this.userServices.forEach(service => {
                if (this.uiMetadata[service.type] === undefined) {
                    console.error('unsupported service type in UI metadata', service.type)
                    throw new Error();
                }  
            })
        }).catch(err => console.error('could not load user info'));
    }

    private loadTestCaseSchema(): Promise<any> {
        return this.http.get(`${this.config.serviceRoot}api/v1/testcases/schema`)
        .toPromise()
        .then((r: EditorResult<SchemaInfo>) => {
            if (r === undefined 
                || r.status_code === undefined 
                || r.status_code !== StatusCode.OK 
                || r.attributes.rules_schema === undefined ) {
                    console.error('empty test case schema endpoint response');
                    throw new Error();
            }
            this.testCaseSchema = r.attributes.rules_schema;

        }).catch(err => console.error('could not load test case schema'));
    }
    
    public getServiceNames(): string[] {
        return Array.from(this.userServicesMap.keys()).sort();
    }
    
    public getUser(): string {
        return this.user;
    }
    
    public getUserServices(): ServiceInfo[] {
        return this.userServices;
    }

    public getUiMetadata(serviceName: string): UiMetadataMap {
        return this.uiMetadata[this.userServicesMap.get(serviceName).type];
    }

    public getServiceInfo(serviceName: string): ServiceInfo {
       return this.userServicesMap.get(serviceName);
    }

    public getConfig(): EditorConfig {
        return this.config;
    }

    public get getBuildInfo(): BuildInfo {
        return this.buildInfo;
    }

    public get environment(): string {
        return this.config.environment;
    }

    public get serviceRoot(): string {
        return this.config.serviceRoot;
    }

    public getTestCaseSchema(): JSONSchema7 {
        return this.testCaseSchema;
    }
}
