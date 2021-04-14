import { TestBed } from '@angular/core/testing';
import { ConfigStoreService } from './config-store.service';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigLoaderService } from '../config-loader.service';
import { of } from 'rxjs';
import { mockTestCaseMap } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { mockUiMetadataMap } from 'testing/uiMetadataMap';
import { mockParserConfig, mockParserConfigCloned } from 'testing/configs';

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
            evaluateTestCase: () => of(mockEvaluateTestCaseMatch),
            submitTestCase: () => of(mockTestCaseMap),
          },
        },
        {
          provide: AppConfigService,
          useValue: {
            uiMetadata: mockUiMetadataMap,
          },
        },
      ],
    });
    configLoader = TestBed.inject(ConfigLoaderService);
    configService = TestBed.inject(AppConfigService);
    service = new ConfigStoreService('parserconfig', 'siembol', configService, configLoader);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('setEditedClonedConfigByName', () => {
    it('should succeed', () => {
      spyOn<any>(service, 'updateEditedConfigAndTestCase');
      spyOn<any>(service, 'getConfigByName').and.returnValue(mockParserConfig);
      service.setEditedClonedConfigByName('config1');
      expect(service['updateEditedConfigAndTestCase']).toHaveBeenCalledOnceWith(mockParserConfigCloned, null);
    });

    it('should fail', () => {
      spyOn<any>(service, 'getConfigByName').and.returnValue(undefined);
      expect(function () {
        service.setEditedClonedConfigByName('config1');
      }).toThrow();
    });
  });
});
