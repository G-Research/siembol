import { Injectable } from '@angular/core';
import { AppConfigService } from '@app/services/app-config.service';
import { ServiceInfo, RepositoryLinks, RepositoryLinksWrapper, UserInfo, SchemaInfo } from '@app/model/config-model';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { JSONSchema7 } from 'json-schema';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { UiMetadata } from '@app/model/ui-metadata-map';
import 'rxjs/add/observable/forkJoin';

export class AppContext {
  user: string;
  userServices: ServiceInfo[];
  userServicesMap: Map<string, ServiceInfo>;
  testCaseSchema: JSONSchema7;
  get serviceNames() {
    return Array.from(this.userServicesMap.keys()).sort();
  }
}

@Injectable({
  providedIn: 'root',
})
export class AppService {
  private appContext: AppContext = new AppContext();
  private loadedSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  public get loaded() {
    return this.loadedSubject.value;
  }
  public get loaded$() {
    return this.loadedSubject.asObservable();
  }
  public get user() {
    return this.appContext.user;
  }
  public get userServices() {
    return this.appContext.userServices;
  }
  public get userServicesMap() {
    return this.appContext.userServicesMap;
  }
  public get testCaseSchema() {
    return this.appContext.testCaseSchema;
  }
  public get serviceNames() {
    return this.appContext.serviceNames;
  }

  constructor(private config: AppConfigService, private http: HttpClient) {}

  public setAppContext(appContext: AppContext): boolean {
    this.appContext = appContext;
    this.loadedSubject.next(true);
    return true;
  }

  public createAppContext(): Observable<AppContext> {
    return Observable.forkJoin([this.loadUserInfo(), this.loadTestCaseSchema()]).map(([appContext, testCaseSchema]) => {
      if (appContext && testCaseSchema) {
        appContext.testCaseSchema = testCaseSchema;
        return appContext;
      }
      throwError('Can not load application context');
    });
  }

  public loadTestCaseSchema(): Observable<JSONSchema7> {
    return this.http.get(`${this.config.serviceRoot}api/v1/testcases/schema`).map((r: SchemaInfo) => {
      if (r === undefined || r.rules_schema === undefined) {
        throwError('empty test case schema endpoint response');
      }
      return r.rules_schema;
    });
  }

  public getRepositoryLinks(serviceName): Observable<RepositoryLinks> {
    return this.http
      .get<RepositoryLinksWrapper>(`${this.config.serviceRoot}api/v1/${serviceName}/configstore/repositories`)
      .pipe(
        map(result => ({
          ...result.rules_repositories,
          service_name: serviceName,
        }))
      );
  }

  public getUiMetadataMap(serviceName: string): UiMetadata {
    const serviceType = this.userServicesMap.get(serviceName).type;
    return this.config.uiMetadata[serviceType];
  }

  public getUserServiceRoles(serviceName: string) {
    return this.appContext.userServicesMap.get(serviceName).user_roles;
  }

  private loadUserInfo(): Observable<AppContext> {
    return this.http.get(`${this.config.serviceRoot}user`).map((r: UserInfo) => {
      if (r === undefined || r.user_name === undefined || r.services === undefined) {
        throwError('empty user endpoint response');
      }

      const ret = new AppContext();
      ret.user = r.user_name;
      ret.userServices = r.services;
      ret.userServicesMap = new Map(ret.userServices.map(x => [x.name, x]));

      ret.userServices.forEach(service => {
        if (this.config.uiMetadata[service.type] === undefined) {
          throwError(`unsupported service type ${service.type} in UI metadata`);
        }
      });
      return ret;
    });
  }
}
