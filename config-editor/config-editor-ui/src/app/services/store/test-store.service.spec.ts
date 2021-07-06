import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TestStoreService } from './test-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { mockStore } from 'testing/store';
import { BehaviorSubject, of } from 'rxjs';
import { mockTestCaseWrapper1, mockTestCaseWrapper2, mockTestCaseMap } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { delay } from 'rxjs/operators';
import { cloneDeep } from 'lodash';
import { ClipboardStoreService } from '../clipboard-store.service';

describe('TestStoreService', () => {
  let configLoader: ConfigLoaderService;
  let service: TestStoreService;
  let clipboardService: ClipboardStoreService;
  beforeEach(() => {
    mockStore.editedConfig.testCases = [cloneDeep(mockTestCaseWrapper1), cloneDeep(mockTestCaseWrapper2)];
    const store = new BehaviorSubject(mockStore);
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
    service = new TestStoreService('siembol', store, configLoader, clipboardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get test case by name', () => {
    expect(service['getTestCaseByName']('test1')).toEqual(mockTestCaseWrapper1);
  });

  it('should update edited test case', () => {
    expect(service['store'].getValue().editedTestCase).toEqual(mockTestCaseWrapper1);
    service.updateEditedTestCase(mockTestCaseWrapper2);
    expect(service['store'].getValue().editedTestCase).toEqual(mockTestCaseWrapper2);
  });

  describe('submitEditedTestCase', () => {
    it('should run', () => {
      service.submitEditedTestCase().subscribe(s => {
        expect(s).toBeTrue();
        expect(service['store'].getValue().editedConfig.testCases).toEqual([
          mockTestCaseWrapper1,
          mockTestCaseWrapper2,
        ]);
      }, fail);
    });
  });

  describe('runEditedTestCase', () => {
    it('should run', () => {
      service.runEditedTestCase();
      expect(service['store'].getValue().editedTestCase.testCaseResult).toEqual(mockEvaluateTestCaseMatch);
    });
  });

  describe('runEditedConfigTestSuite', () => {
    it('should run test suite', fakeAsync(() => {
      expect(service['store'].getValue().editedConfig.testCases[1].testCaseResult).toEqual(null);
      spyOn(configLoader, 'evaluateTestCase').and.returnValue(of(mockEvaluateTestCaseMatch).pipe(delay(1)));
      service.runEditedConfigTestSuite();
      tick(1);
      expect(service['store'].getValue().editedConfig.testCases[1].testCaseResult).toEqual(mockEvaluateTestCaseMatch);
    }));
  });
});
