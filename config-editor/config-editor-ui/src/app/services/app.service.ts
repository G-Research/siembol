import { Injectable } from '@angular/core';
import { AppConfigService } from '@app/services/app-config.service';
import { ServiceInfo, RepositoryLinks, RepositoryLinksWrapper, UserInfo, UserRole, SchemaInfo, Application, applications } from '@app/model/config-model';
import { Observable, throwError, BehaviorSubject, forkJoin, of } from 'rxjs';
import { JSONSchema7 } from 'json-schema';
import { HttpClient } from '@angular/common/http';
import { UiMetadata } from '@app/model/ui-metadata-map';
import { map, mergeMap } from 'rxjs/operators';
import { ServiceContextMap } from '@app/model/app-config';
import { ServiceContext } from './editor.service';

export class AppContext {
  user: string;
  userServices: ServiceInfo[];
  userServicesMap: Map<string, ServiceInfo>;
  testCaseSchema: JSONSchema7;
  repositoryLinks: { [name: string]: RepositoryLinks };
  isAdminOfAnyService: boolean;
  serviceContextMap: ServiceContextMap = {};
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
  get repositoryLinks() {
    return this.appContext.repositoryLinks;
  }
  get isAdminOfAnyService() {
    return this.appContext.isAdminOfAnyService;
  }

  constructor(private config: AppConfigService, private http: HttpClient) {}

  setAppContext(appContext: AppContext): boolean {
    this.appContext = appContext;
    this.loadedSubject.next(true);
    return true;
  }

  createAppContext(): Observable<AppContext> {
    return this.loadUserInfo()
      .pipe(
        mergeMap((appContext: AppContext) => 
          forkJoin(
            this.loadTestCaseSchema(),
            this.getAllRepositoryLinks(appContext.userServices),
            of(appContext)
            )
      )).pipe(map(([testCaseSchema, repositoryLinks, appContext]) => {
        if (appContext && testCaseSchema && repositoryLinks) {
          appContext.testCaseSchema = testCaseSchema;
          appContext.repositoryLinks = repositoryLinks;
          appContext.isAdminOfAnyService = this.isUserAdminOfAnyService(appContext.userServices);
          return appContext;
        }
        throwError('Can not load application context');
      }));
  }

  loadTestCaseSchema(): Observable<JSONSchema7> {
    return this.http.get(`${this.config.serviceRoot}api/v1/testcases/schema`).pipe(map((r: SchemaInfo) => {
      if (r === undefined || r.rules_schema === undefined) {
        throwError('empty test case schema endpoint response');
      }
      return r.rules_schema;
    }));
  }

  getUiMetadataMap(serviceName: string): UiMetadata {
    const serviceType = this.userServicesMap.get(serviceName).type;
    return this.config.uiMetadata[serviceType];
  }

  getUserServiceRoles(serviceName: string): UserRole[] {
    return this.appContext.userServicesMap.get(serviceName).user_roles;
  }

  restartApplication(serviceName: string, application: string): Observable<Application[]> {
    return this.http.post<applications>(
      `${this.config.serviceRoot}api/v1/${serviceName}/topologies/${application}/restart`,
      null
    ).pipe(map(result => result.topologies));
  }

  restartAllApplications(): Observable<Application[]> {
    return this.http.post<applications>(
      `${this.config.serviceRoot}api/v1/topologies/restart`,
      null
    ).pipe(map(result => result.topologies));
  }

  getAllApplications(): Observable<Application[]> {
    return forkJoin(
      this.userServices
      .filter(userService => userService.user_roles.includes(UserRole.SERVICE_ADMIN))
      .map(userService => this.getServiceApplications(userService.name))
    ).pipe(map(topologies => topologies.flat()));
  }

  getServiceRepositoryLink(serviceName: string): RepositoryLinks {
    return this.appContext.repositoryLinks[serviceName];
  }

  updateServiceContextMap(serviceContext: ServiceContext) {
    this.appContext.serviceContextMap[serviceContext.serviceName] = serviceContext;
  }

  getServiceContext(serviceName: string): ServiceContext {
    if (serviceName in this.appContext.serviceContextMap) {
      return this.appContext.serviceContextMap[serviceName];
    }
    throwError(() => `Service: ${serviceName} does not exist in context map.`);
  }

  private isUserAdminOfAnyService(userServices: ServiceInfo[]): boolean {
    for (const userService of userServices) {
      if (userService.user_roles.includes(UserRole.SERVICE_ADMIN)) {
        return true;
      }
    }
    return false;
  }

  private getServiceApplications(serviceName: string): Observable<Application[]> {
    return this.http.get<applications>(
      `${this.config.serviceRoot}api/v1/${serviceName}/topologies`
    ).pipe(map(result => result.topologies));
  }
  
  private getAllRepositoryLinks(userServices: ServiceInfo[]): Observable<{ [name: string]: RepositoryLinks }> {
    return forkJoin(userServices.map(x => this.getRepositoryLinks(x.name)))
            .pipe(map((links: RepositoryLinks[]) => {
                if (links) {
                    return links.reduce((pre, cur) => ({ ...pre, [cur.service_name]: cur }), {});
                }
            }));
  }

  private getRepositoryLinks(serviceName: string): Observable<RepositoryLinks> {
    return this.http
      .get<RepositoryLinksWrapper>(`${this.config.serviceRoot}api/v1/${serviceName}/configstore/repositories`)
      .pipe(map(result => ({
          ...result.rules_repositories,
          service_name: serviceName,
        })
      ));
  }

  private loadUserInfo(): Observable<AppContext> {
    return this.http.get(`${this.config.serviceRoot}user`).pipe(map((r: UserInfo) => {
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
    }));
  }
}
