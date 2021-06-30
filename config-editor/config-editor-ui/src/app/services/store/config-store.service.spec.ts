import { TestBed } from '@angular/core/testing';
import { ConfigStoreService } from './config-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { of } from 'rxjs';
import { mockTestCaseMap } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { mockUiMetadataParser } from 'testing/uiMetadataMap';
import { mockParserConfig, mockParserConfigCloned } from 'testing/configs';

describe('ConfigStoreService', () => {
  let configLoader: ConfigLoaderService;
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
      ],
    });
    configLoader = TestBed.inject(ConfigLoaderService);
    service = new ConfigStoreService('siembol', mockUiMetadataParser, configLoader);
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
