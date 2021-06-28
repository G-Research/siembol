import { TestBed } from '@angular/core/testing';
import { ConfigStoreService } from './config-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { of } from 'rxjs';
import { mockTestCaseMap } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { mockUiMetadataParser } from 'testing/uiMetadataMap';
import { mockParserConfig, mockParserConfigCloned } from 'testing/configs';
import { ClipboardStoreService } from '../clipboard-store.service';

describe('ConfigStoreService', () => {
  let configLoader: ConfigLoaderService;
  let service: ConfigStoreService;
  let clipboardService: ClipboardStoreService;
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
          provide: ClipboardStoreService,
          useValue: jasmine.createSpy(),
        },
      ],
    });
    configLoader = TestBed.inject(ConfigLoaderService);
    clipboardService = TestBed.inject(ClipboardStoreService);
    service = new ConfigStoreService('siembol', mockUiMetadataParser, configLoader, clipboardService);
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
