import { EditorService } from "@app/services/editor.service";
import { TestStoreService } from "@app/services/store/test-store.service";
import { of } from "rxjs";
import { mockParserConfig } from "testing/configs";
import { ConfigEditGuard } from ".";

describe('ConfigEditGuard', () => {
    let guard: ConfigEditGuard;
    let store;
    let testStore: TestStoreService;
    let service: EditorService;
    beforeEach(() => {
      testStore = jasmine.createSpyObj
      (
        'TestStoreService',
        {
          setEditedTestCaseNew: () => true,
          setEditedClonedTestCaseByName: () => true,
          setEditedPastedTestCaseNew: () => true,
        },
        []
      );
      store = jasmine.createSpyObj
      (
        'ConfigStoreService', 
        {
          setEditedConfigAndTestCaseByName: () => true, 
          setEditedClonedConfigByName: () => true,
          setNewEditedPastedConfig: () => true,
        }, 
        {
          editedConfig$: of(mockParserConfig),
          testService: testStore,
        }
      );
      service = jasmine.createSpyObj('EditorService', [], {configStore: store});
      guard = new ConfigEditGuard(service);
    });
  
    it('should create', () => {
      expect(guard).toBeTruthy();
    });

    it('load config', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {configName: "test"}});
      guard.canActivate(route);
      expect(store.setEditedConfigAndTestCaseByName).toHaveBeenCalledOnceWith("test", undefined);
    });

    it('clone config', () => {
      const route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], { queryParams: {cloneConfig: "test"}});
      guard.canActivate(route);
      expect(store.setEditedClonedConfigByName).toHaveBeenCalledOnceWith("test");
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
});