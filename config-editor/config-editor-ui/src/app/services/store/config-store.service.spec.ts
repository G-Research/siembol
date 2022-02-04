import { TestBed } from '@angular/core/testing';
import { ConfigStoreService } from './config-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { mockParserConfig, mockParserConfigData } from 'testing/configs';
import { mockUiMetadataParser } from 'testing/uiMetadataMap';
import { AppConfigService } from '../app-config.service';
import { cloneDeep } from 'lodash';
import { mockTestCaseMap } from 'testing/testcases';
import { of } from 'rxjs';

const expectedClonedConfig = {
  author: "siembol",
  configData: Object.assign({}, cloneDeep(mockParserConfigData), {
    parser_name : "test_clone",
    parser_version: 0,
  }),
  description: undefined,
  isNew: true,
  name: "test_clone",
  savedInBackend: false,
  testCases: [],
  version: 0,
};

describe('ConfigStoreService', () => {
  let configLoader: ConfigLoaderService;
  let configService: AppConfigService;
  let service: ConfigStoreService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ConfigLoaderService,
          useValue: {
            submitConfig: () => of([mockParserConfig]),
          },
        },
        {
          provide: AppConfigService,
          useValue:  {
            useImporters: false,
          },
        },
      ],
    });
    configLoader = TestBed.inject(ConfigLoaderService);
    configService = TestBed.inject(AppConfigService);
    service = new ConfigStoreService('siembol', mockUiMetadataParser, configLoader, configService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get cloned config and test cases', () => {
    spyOn(service['store'], 'getValue').and.returnValue({
      configs: [cloneDeep(mockParserConfig)],
      testCaseMap: cloneDeep(mockTestCaseMap),
    } as any);
    const spy = spyOn(service.testService, 'getClonedTestCase').and.returnValues(
      {name: "test1"},
      {name: "test2"}
    );
    const toClone = service.getClonedConfigAndTestsByName("config1", "test_clone", true);
    expect(spy).toHaveBeenCalledTimes(2);
    expect(toClone).toEqual({
      config: expectedClonedConfig,
      test_cases: [
        {name: "test1"},
        {name: "test2"},
      ]} as any
    )
  })

  it('should get cloned config only', () => {
    spyOn(service['store'], 'getValue').and.returnValue({
      configs: [cloneDeep(mockParserConfig)],
      testCaseMap: cloneDeep(mockTestCaseMap),
    } as any);
    const toClone = service.getClonedConfigAndTestsByName("config1", "test_clone", false);
    expect(toClone).toEqual({
      config: expectedClonedConfig,
      test_cases: [],
    }
    )
  })

  it('should submit clone config and test cases', done => {
    const to_clone = {
      config: expectedClonedConfig,
      test_cases: [
        {name: "test1"},
        {name: "test2"},
      ],
    } as any;
    const spy_test_service = spyOn(service['testStoreService'], 'submitTestCases').and.returnValue(of(mockTestCaseMap));
    const spy = spyOn(service, 'setConfigAndTestCasesInStore').and.returnValue(true);
    service.submitClonedConfigAndTests(to_clone).subscribe(() => {
      expect(spy_test_service).toHaveBeenCalledTimes(1);
      expect(spy).toHaveBeenCalledOnceWith([mockParserConfig], mockTestCaseMap);
      done();
    })
  });
});

