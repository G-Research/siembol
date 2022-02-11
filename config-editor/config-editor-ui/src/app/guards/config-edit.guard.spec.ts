import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { EditorService } from "@app/services/editor.service";
import { TestStoreService } from "@app/services/store/test-store.service";
import { of } from "rxjs";
import { mockParserConfig } from "testing/configs";
import { ConfigEditGuard } from ".";

describe('ConfigEditGuard', () => {
    let guard: ConfigEditGuard;
    let store;
    let testStore: TestStoreService;
    let router: Router;
    let service: EditorService;
    beforeEach(() => {
      testStore = jasmine.createSpyObj(
        'TestStoreService',
        {
          setEditedTestCaseNew: () => true,
          setEditedClonedTestCaseByName: () => true,
          setEditedPastedTestCaseNew: () => true,
        },
        []
      );
      store = jasmine.createSpyObj(
        'ConfigStoreService',
        {
          setEditedConfigAndTestCaseByName: () => true, 
          setNewEditedPastedConfig: () => true,
          setClonedConfigAndTestsFromOtherService: () => of(true),
          setClonedConfigAndTests: () => of(true),
        }, 
        {
          editedConfig$: of(mockParserConfig),
          testService: testStore,
        }
      );
      TestBed.configureTestingModule({
        imports: [
          RouterTestingModule,
        ],
      });
      service = jasmine.createSpyObj('EditorService', [], {configStore: store, serviceName: "test_parser"});
      router = TestBed.inject(Router);
      guard = new ConfigEditGuard(service, router);
    });
  
    it('should create', () => {
      expect(guard).toBeTruthy();
    });

    it('load config', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", undefined);
    });

    it('paste config', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {pasteConfig: "true"}});
      guard.canActivate(route);
      expect(store.setNewEditedPastedConfig).toHaveBeenCalled();
    });

    it('load config and test case', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test", testCaseName: "test1"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", "test1");
    });

    it('load config and clone test case', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test", cloneTestCase: "test1"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", undefined);
      expect(testStore.setEditedClonedTestCaseByName).toHaveBeenCalledOnceWith("test1");
    });

    it('load config and new test case', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test", newTestCase: "true"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", undefined);
      expect(testStore.setEditedTestCaseNew).toHaveBeenCalled();
    });

    it('load config and paste test case', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test", pasteTestCase: "true"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", undefined);
      expect(testStore.setEditedPastedTestCaseNew).toHaveBeenCalled();
    });

    it('clone config other service', () => {
      store.setClonedConfigAndTestsFromOtherService.and.returnValue(of(true));
      const route = jasmine.createSpyObj(
        'ActivatedRouteSnapshot', [], 
        { 
          queryParams: 
          {
            cloneConfig: "test",
            withTestCases: true,
            newConfigName: "test_clone",
            fromService: "alert",
          },
        });
        guard.canActivate(route);
        expect(store.setClonedConfigAndTestsFromOtherService).toHaveBeenCalledOnceWith("test", 'test_clone', true, 'alert' );
    });

    it('clone config same service', () => {
      store.setClonedConfigAndTests.and.returnValue(of(true));
      const route = jasmine.createSpyObj(
        'ActivatedRouteSnapshot', [], 
        { 
          queryParams: 
          {
            cloneConfig: "test",
            withTestCases: true,
            newConfigName: "test_clone",
            fromService: "test_parser",
          },
        });
        guard.canActivate(route);
        expect(store.setClonedConfigAndTests).toHaveBeenCalledOnceWith("test", 'test_clone', true);
    });
});