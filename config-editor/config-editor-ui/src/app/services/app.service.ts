import { Injectable } from '@angular/core';
import { AppConfigService } from '@app/services/app-config.service';
import { ServiceInfo, RepositoryLinks, RepositoryLinksWrapper, UserInfo, SchemaInfo } from '@app/model/config-model';
import { Observable, throwError, BehaviorSubject, forkJoin } from 'rxjs';
import { JSONSchema7 } from 'json-schema';
import { HttpClient } from '@angular/common/http';
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
  private repositoryLinks: { [name: string]: RepositoryLinks } = {}
  private loadedSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  get loaded() {
    return this.loadedSubject.value;
  }
  get loaded$() {
    return this.loadedSubject.asObservable();
  }
  get user() {
    return this.appContext.user;
  }
  get userServices() {
    return this.appContext.userServices;
  }
  get userServicesMap() {
    return this.appContext.userServicesMap;
  }
  get testCaseSchema() {
    return this.appContext.testCaseSchema;
  }
  get serviceNames() {
    return this.appContext.serviceNames;
  }
  get allRepositoryLinks() {
    return this.repositoryLinks;
  }

  constructor(private config: AppConfigService, private http: HttpClient) {}

  setAppContextandInitialise(appContext: AppContext): Observable<boolean> {
    this.appContext = appContext;
    return this.getAllRepositoryLinks().map(() => {
      this.loadedSubject.next(true);
      return true
    });
    ;
  }

  createAppContext(): Observable<AppContext> {
    return forkJoin([this.loadUserInfo(), this.loadTestCaseSchema()]).map(([appContext, testCaseSchema]) => {
      if (appContext && testCaseSchema) {
        appContext.testCaseSchema = testCaseSchema;
        return appContext;
      }
      throwError('Can not load application context');
    });
  }

  loadTestCaseSchema(): Observable<JSONSchema7> {
    return this.http.get(`${this.config.serviceRoot}api/v1/testcases/schema`).map((r: SchemaInfo) => {
      if (r === undefined || r.rules_schema === undefined) {
        throwError('empty test case schema endpoint response');
      }
      return r.rules_schema;
    });
  }

  getUiMetadataMap(serviceName: string): UiMetadata {
    const serviceType = this.userServicesMap.get(serviceName).type;
    return this.config.uiMetadata[serviceType];
  }

  getUserServiceRoles(serviceName: string) {
    return this.appContext.userServicesMap.get(serviceName).user_roles;
  }

  getServiceRepositoryLink(serviceName: string) {
    return this.repositoryLinks[serviceName];
  }

  private getAllRepositoryLinks(): Observable<void> {
    return forkJoin(this.userServices.map(x => this.getRepositoryLinks(x.name)))
            .map((links: RepositoryLinks[]) => {
                if (links) {
                    this.repositoryLinks = links.reduce((pre, cur) => ({ ...pre, [cur.service_name]: cur }), {});
                }
            })
  }

  private getRepositoryLinks(serviceName: string): Observable<RepositoryLinks> {
    return this.http
      .get<RepositoryLinksWrapper>(`${this.config.serviceRoot}api/v1/${serviceName}/configstore/repositories`)
      .map(result => ({
          ...result.rules_repositories,
          service_name: serviceName,
        })
      );
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
