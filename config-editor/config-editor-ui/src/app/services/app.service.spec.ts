import { TestBed } from '@angular/core/testing';
import { AppConfigService } from '@app/services/app-config.service';
import { AppService } from './app.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { mockAppContext, mockUserServicesMap, mockAppContextWithTestSchema } from 'testing/appContext';
import { mockUserInfo } from 'testing/user';
import { mockUiMetadataMap } from 'testing/uiMetadataMap';
import { UserRole } from '@app/model/config-model';
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

  it('should create app context', () => {
    spyOn<any>(service, 'loadUserInfo').and.returnValue(of(cloneDeep(mockAppContext)));
    spyOn<any>(service, 'loadTestCaseSchema').and.returnValue(of(mockTestCasesSchema));
    service.createAppContext().subscribe(a => expect(a).toEqual(mockAppContextWithTestSchema), fail);
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
