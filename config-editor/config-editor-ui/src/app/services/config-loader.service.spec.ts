import { TestBed } from '@angular/core/testing';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigLoaderService } from './config-loader.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { mockUiMetadataParser } from 'testing/uiMetadataMap';
import {
  mockEvaluateTestCaseMatch,
  mockConfigTestResultMatch,
  mockEvaluationResultMatch,
  mockEvaluateTestCaseFromResult,
} from 'testing/testCaseResults';
import { mockRelease } from 'testing/release';
import { mockDeploymentWrapper, mockDeployment } from 'testing/deploymentWrapper';
import { mockTestCaseMap, mockTestCaseWrapper1, mockTestCase1 } from 'testing/testcases';
import { mockTestCaseFiles } from 'testing/testCaseFiles';
import { mockParserConfig, mockParserConfigFiles, mockParserConfigData, mockParserConfigFile } from 'testing/configs';
import { mockAdminConfig, mockAdminConfigFiles } from 'testing/adminconfigs';
import { of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

describe('ConfigLoaderService', () => {
  let httpTestingController: HttpTestingController;
  let httpClient: HttpClient;
  let appConfigService: AppConfigService;
  let service: ConfigLoaderService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: AppConfigService,
          useValue: jasmine.createSpyObj('AppConfigService', [], {
            serviceRoot: '/',
          }),
        },
      ],
    });
    httpTestingController = TestBed.inject(HttpTestingController);
    appConfigService = TestBed.inject(AppConfigService);
    httpClient = TestBed.inject(HttpClient);
    service = new ConfigLoaderService(httpClient, appConfigService, 'test', mockUiMetadataParser);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('evaluate test case', () => {
    spyOn(service, 'testSingleConfig').and.returnValue(of(mockConfigTestResultMatch));
    spyOn(service, 'evaluateTestCaseFromResult').and.returnValue(of(mockEvaluationResultMatch));
    service.evaluateTestCase({}, mockTestCaseWrapper1).subscribe(s => {
      expect(s).toEqual(mockEvaluateTestCaseMatch);
    }, fail);
  });

  it('evaluate test case from result', () => {
    service.evaluateTestCaseFromResult(mockTestCase1, { test: null }).subscribe();
    const req = httpTestingController.expectOne('/api/v1/testcases/evaluate');
    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(mockEvaluateTestCaseFromResult);
  });

  it('should get admin schema', () => {
    service.getAdminSchema().subscribe(s => expect(s).toEqual({}), fail);

    const req = httpTestingController.expectOne('/api/v1/test/adminconfig/schema');
    expect(req.request.method).toEqual('GET');
    req.flush({ admin_config_schema: {} });
  });

  it('should get release', () => {
    service.getRelease().subscribe(s => expect(s).toEqual(mockDeploymentWrapper), fail);

    const req = httpTestingController.expectOne('/api/v1/test/configstore/release');
    expect(req.request.method).toEqual('GET');
    req.flush(mockRelease);
  });

  it('should convert testcase files to map', () => {
    expect(service['testCaseFilesToMap'](mockTestCaseFiles)).toEqual(mockTestCaseMap);
  });

  it('should submit config', () => {
    service.submitConfig(mockParserConfig).subscribe(s => {
      expect(s).toEqual([mockParserConfig]);
    }, fail);

    const req = httpTestingController.expectOne('/api/v1/test/configstore/configs');
    expect(req.request.method).toEqual('PUT');
    req.flush(mockParserConfigFiles);
  });

  it('should marshal deployment format', () => {
    expect(service['marshalDeploymentFormat'](mockDeployment)).toEqual({
      parsers_configurations: [mockParserConfigData],
      parsers_version: 1,
    });
  });

  it('should get config from file', () => {
    expect(service['getConfigFromFile'](mockParserConfigFile)).toEqual(mockParserConfig);
  });

  it('should get admin config', () => {
    service.getAdminConfig().subscribe(s => expect(s).toEqual(mockAdminConfig), fail);

    const req = httpTestingController.expectOne('/api/v1/test/configstore/adminconfig');
    expect(req.request.method).toEqual('GET');
    req.flush(mockAdminConfigFiles);
  });
});
