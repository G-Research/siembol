import { TestBed } from '@angular/core/testing';
import { ConfigStoreService } from './config-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { of } from 'rxjs';
import { mockTestCaseMap } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { mockUiMetadataParser } from 'testing/uiMetadataMap';
import { AppConfigService } from '../app-config.service';

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
});
