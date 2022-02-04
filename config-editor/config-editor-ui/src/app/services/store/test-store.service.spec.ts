/* eslint-disable no-unused-vars */
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TestStoreService } from './test-store.service';
import { ConfigLoaderService } from '../config-loader.service';
import { mockStore } from 'testing/store';
import { BehaviorSubject, of } from 'rxjs';
import { mockTestCaseWrapper1, mockTestCaseWrapper2, mockTestCaseMap, mockTestCaseWrapper3, mockTestCase1 } from 'testing/testcases';
import { mockEvaluateTestCaseMatch } from 'testing/testCaseResults';
import { delay } from 'rxjs/operators';
import { cloneDeep } from 'lodash';
import { ClipboardStoreService } from '../clipboard-store.service';
import { TestScheduler } from 'rxjs/testing';
// import { cold } from 'jasmine-marbles';

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

  describe('submitTestCases', () => {
    let testScheduler: TestScheduler;

    beforeEach(() => {
      testScheduler = new TestScheduler((actual, expected) => {
        expect(actual).toEqual(expected);
      });
    });

    it('should submit test cases in order', ()=> {
      const mockTestCaseWrappers = [mockTestCaseWrapper1, mockTestCaseWrapper2, mockTestCaseWrapper3];
      testScheduler.run(({ cold, expectObservable }) => {
        const spy = spyOn(configLoader, 'submitTestCase').and.returnValues(
          cold(`----a|`,  {a: "first" as any}), 
          cold(`--a|`, {a: "second" as any}), 
          cold('a|',  {a: "third" as any})
        );
        const testCaseMap$ = service.submitTestCases(mockTestCaseWrappers);
        expect(spy).toHaveBeenCalledTimes(3);
        expect(spy).toHaveBeenCalledWith(mockTestCaseWrapper1);
        expect(spy).toHaveBeenCalledWith(mockTestCaseWrapper2);
        expect(spy).toHaveBeenCalledWith(mockTestCaseWrapper3);
        expectObservable(testCaseMap$).toBe('---------(a|)',  {a: "third"});
      })
    });
  })

  it("should return cloned test case", () => {
    const clone = service.getClonedTestCase(mockTestCaseWrapper1, "config_name");
    const mockTestCase1Clone = cloneDeep(mockTestCase1);
    mockTestCase1Clone.config_name = "config_name";
    mockTestCase1Clone.version = 0;
    const expected = { 
      fileHistory: null, 
      testCaseResult: null, 
      testCase: mockTestCase1Clone,
    }
    expect(clone).toEqual(expected);
  })
});
