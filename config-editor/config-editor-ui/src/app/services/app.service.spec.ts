import { TestBed } from '@angular/core/testing';
import { AppConfigService } from '@app/services/app-config.service';
import { AppService } from './app.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { mockAppContext, mockUserServicesMap } from 'testing/appContext';
import { mockUserInfo } from 'testing/user';
import { mockUiMetadataMap } from 'testing/uiMetadataMap';
import { RepositoryLinks, UserRole } from '@app/model/config-model';
import { mockTestCasesSchema } from 'testing/testCasesSchema';
import { of } from 'rxjs';
import { cloneDeep } from 'lodash';

const mockTopology1 = 
{ 
  image: "test-image-alert",
  attributes: ["test"],
  topology_name: "myalert1",
  topology_id: "123",
  service_name: "myalert",
}

const mockTopology2 =
{ 
  image: "test-image-parsers",
  attributes: ["test"],
  topology_name: "myparserconfig1",
  topology_id: "456",
  service_name: "myparserconfig",
}

const mockTopology3 =
{ 
  image: "test-image-parsers",
  attributes: ["test"],
  topology_name: "myparserconfig2",
  topology_id: "789",
  service_name: "myparserconfig",
}

const mockTopologies1 = {
  topologies: [
    mockTopology1,
  ],
}

const mockTopologies2 = {
  topologies: [
    mockTopology2,
    mockTopology3,
  ],
}

const mockRepositories1 = {
  "rules_repositories": {
    "rule_store_url": "https://github.com/test-siembol-config.git",
    "test_case_store_url": "https://github.com/test-siembol-config.git",
    "rules_release_url": "https://github.com/test-siembol-config.git",
    "admin_config_url": "https://github.com/test-siembol-config.git",
    "rule_store_directory_url": "https://github.com/test-siembol-config/tree/main/alert/rules",
    "test_case_store_directory_url": "https://github.com/test-siembol-config/tree/main/alert/testcases",
    "rules_release_directory_url": "https://github.com/test-siembol-config/tree/main/alert/release",
    "admin_config_directory_url": "https://github.com/test-siembol-config/tree/main/alert/adminconfig",
  },
}

const mockRepositories2 = {
  "rules_repositories": {
    "rule_store_url": "https://github.com/test-siembol-config.git",
    "test_case_store_url": "https://github.com/test-siembol-config.git",
    "rules_release_url": "https://github.com/test-siembol-config.git",
    "rule_store_directory_url": "https://github.com/test-siembol-config/tree/main/myparserconfig/rules",
    "test_case_store_directory_url": "https://github.com/test-siembol-config/tree/main/myparserconfig/testcases",
    "rules_release_directory_url": "https://github.com/test-siembol-config/tree/main/myparserconfig/release",
  },
}

const expectedAllRepositoriesLinks: {[name: string]: RepositoryLinks} = {
  "myalert": { ...mockRepositories1.rules_repositories, ...{service_name: "myalert"}} ,
  "myparserconfig": { ...mockRepositories2.rules_repositories, ...{service_name: "myparserconfig"}},
}
describe('AppService', () => {
  let httpTestingController: HttpTestingController;
  let service: AppService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AppService,
        {
          provide: AppConfigService,
          useValue: jasmine.createSpyObj('AppConfigService', [], {
            serviceRoot: '/',
            uiMetadata: mockUiMetadataMap,
          }),
        },
      ],
    });
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AppService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load user info', () => {
    service['loadUserInfo']().subscribe(i => expect(i).toEqual(mockAppContext), fail);

    const req = httpTestingController.expectOne('/user');
    expect(req.request.method).toEqual('GET');
    req.flush(mockUserInfo);
  });

  it('should get user service roles', () => {
    service['appContext'].userServicesMap = mockUserServicesMap;
    expect(service.getUserServiceRoles('myalert')).toEqual([UserRole.SERVICE_USER, UserRole.SERVICE_ADMIN]);
  });

  it('should create app context', done => {
    spyOn<any>(service, 'loadUserInfo').and.returnValue(of(cloneDeep(mockAppContext)));
    spyOn<any>(service, 'loadTestCaseSchema').and.returnValue(of(mockTestCasesSchema));
    service.createAppContext().subscribe(context => {
      expect(context.repositoryLinks).toEqual(expectedAllRepositoriesLinks);
      done();
    });

    const req1 = httpTestingController.expectOne('/api/v1/myalert/configstore/repositories');
    expect(req1.request.method).toEqual('GET');
    req1.flush(mockRepositories1);

    const req2 = httpTestingController.expectOne('/api/v1/myparserconfig/configstore/repositories');
    expect(req2.request.method).toEqual('GET');
    req2.flush(mockRepositories2);
  });

  it('should get all applications', done => {
    service.setAppContext(mockAppContext);
    service.getAllApplications().subscribe(apps => {
      expect(apps).toEqual([mockTopology1, mockTopology2, mockTopology3]);
      done()
    })

    const req1 = httpTestingController.expectOne('/api/v1/myalert/topologies');
    expect(req1.request.method).toEqual('GET');
    req1.flush(mockTopologies1);

    const req2 = httpTestingController.expectOne('/api/v1/myparserconfig/topologies');
    expect(req2.request.method).toEqual('GET');
    req2.flush(mockTopologies2);
  })
});
