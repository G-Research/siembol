import { Injectable } from '@angular/core';
import { AppConfigService } from '../config';
import {
    ServiceInfo, RepositoryLinks, EditorResult,
    RepositoryLinksWrapper, UserInfo, SchemaInfo
} from '@app/model/config-model';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { JSONSchema7 } from 'json-schema';
import { HttpClient } from '@angular/common/http';
import { StatusCode } from '@app/commons';
import { map } from 'rxjs/operators';
import { UiMetadataMap } from '@app/model/ui-metadata-map';

export class AppContext {
    user: string;
    userServices: ServiceInfo[];
    userServicesMap: Map<string, ServiceInfo>;
    testCaseSchema: JSONSchema7;
    get serviceNames() { return Array.from(this.userServicesMap.keys()).sort(); }
    constructor() { }
}

@Injectable({
    providedIn: 'root',
})
export class AppService {
    private appContext: AppContext = new AppContext();
    private loadedSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    
    public get loaded() { return this.loadedSubject.value; }
    public get loaded$() { return this.loadedSubject.asObservable(); }
    public get user() { return this.appContext.user; }
    public get userServices() { return this.appContext.userServices; }
    public get userServicesMap() { return this.appContext.userServicesMap; }
    public get testCaseSchema() { return this.appContext.testCaseSchema; }
    public get serviceNames() { return this.appContext.serviceNames; }
    
    constructor(private config: AppConfigService, private http: HttpClient) { }

    public setAppContext(appContext: AppContext): boolean {
        this.appContext = appContext;
        this.loadedSubject.next(true);
        return true;
    }

    public createAppContext(): Observable<AppContext> {
        return Observable.forkJoin([
            this.loadUserInfo(),
            this.loadTestCaseSchema()])
            .map(([appContext, testCaseSchema]) => {
                if (appContext && testCaseSchema) {
                    appContext.testCaseSchema = testCaseSchema;
                    return appContext;
                } else {
                    throwError('Can not load application context');
                }
            });
    }

    private loadUserInfo(): Observable<AppContext> {
        return this.http.get(`${this.config.serviceRoot}user`)
            .map((r: EditorResult<UserInfo>) => {
                if (r === undefined
                    || r.status_code === undefined
                    || r.status_code !== StatusCode.OK
                    || r.attributes.user_name === undefined
                    || r.attributes.services === undefined) {
                    throwError('empty user endpoint response');
                }

                const ret = new AppContext();
                ret.user = r.attributes.user_name;
                ret.userServices = r.attributes.services;
                ret.userServicesMap = new Map(ret.userServices.map(x => [x.name, x]));

                ret.userServices.forEach(service => {
                    if (this.config.uiMetadata[service.type] === undefined) {
                        throwError(`unsupported service type ${service.type} in UI metadata`);
                    }
                });
                return ret;
            });
    }

    public loadTestCaseSchema(): Observable<JSONSchema7> {
        return this.http.get(`${this.config.serviceRoot}api/v1/testcases/schema`)
            .map((r: EditorResult<SchemaInfo>) => {
                if (r === undefined
                    || r.status_code === undefined
                    || r.status_code !== StatusCode.OK
                    || r.attributes.rules_schema === undefined) {
                    throwError('empty test case schema endpoint response');
                }
                return r.attributes.rules_schema;
            });
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

    public getUiMetadataMap(serviceName: string): UiMetadataMap {
        const serviceType = this.userServicesMap.get(serviceName).type;
        return this.config.uiMetadata[serviceType]; 
    }
}
