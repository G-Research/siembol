import { TestBed, waitForAsync } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ConfigManagerComponent } from './config-manager.component';
import { of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { cold, getTestScheduler } from 'jasmine-marbles';
import { EditorService } from '@app/services/editor.service';
import { mockUiMetadataAlert } from 'testing/uiMetadataMap';
import { AppConfigService } from '@app/services/app-config.service';
import { PopupService } from '@app/services/popup.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { By } from '@angular/platform-browser';
import {MatMenuHarness} from '@angular/material/menu/testing';
import {TestbedHarnessEnvironment} from '@angular/cdk/testing/testbed';
import {MatButtonHarness} from '@angular/material/button/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

class MatDialogMock {
  open() {
    return {
      afterClosed: () => of(true)
    }
  }
}

function spyPropertyGetter<T, K extends keyof T>(
  spyObj: jasmine.SpyObj<T>,
  propName: K
): jasmine.Spy<() => T[K]> {
  return Object.getOwnPropertyDescriptor(spyObj, propName)?.get as jasmine.Spy<() => T[K]>;
}

fdescribe('ConfigManagerComponent', () => {
  let mockRouter: any;
  let mockEditorService: any;
  let mockConfigStore: any;
  let mockAppConfigService: any;

  beforeEach(waitForAsync(() => {
    const mockPopupService = jasmine.createSpyObj('PopupService', ['openNotification']);
    const mockActivatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {'queryParamMap': cold('x|', {x: { 'filter': ['general|unreleased']}})});
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', [], ['useImporters', 'blockingTimeout']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockConfigStore = jasmine.createSpyObj(
      'ConfigStoreService',
      ['upgradeConfigInRelease', 'incrementChangesInRelease', 'moveConfigInRelease', 'addConfigToRelease', 'removeConfigFromRelease', 'loadPullRequestStatus', 'submitRelease', 'resetChangesInRelease', 'reloadStoreAndRelease', 'deleteConfig'],
      ['sortedConfigs$', 'pullRequestPending$', 'releaseSubmitInFlight$', 'searchTerm$', 'release$', 'releaseHistory$', 'importers$', 'configManagerRowData$', 'isAnyFilterPresent$', 'countChangesInRelease$', 'serviceFilters$', 'serviceFilterConfig$', 'clipboardService']
    );
    
    mockEditorService = jasmine.createSpyObj(
      'EditorService',
      ['getLatestFilters', 'onSaveSearch', 'onDeleteSearch'],
      { 
        'serviceName': 'test', 
        'metaDataMap': mockUiMetadataAlert, 
        'configStore': mockConfigStore, 
        'searchHistoryService': { getSearchHistory: () => undefined }
      }
    );
  
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialog, useClass: MatDialogMock },
        { provide: EditorService, useValue: mockEditorService },
        { provide: AppConfigService, useValue: mockAppConfigService},
        { provide: PopupService, useValue: mockPopupService},
        { provide: ActivatedRoute, useValue: mockActivatedRoute},
        { provide: Router, useValue: mockRouter},
      ],
      declarations: [
        ConfigManagerComponent
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    // values needed in constructor and onInit
    spyPropertyGetter(mockAppConfigService, 'useImporters').and.returnValue(true);
    spyPropertyGetter(mockConfigStore, 'pullRequestPending$').and.returnValue(cold('x', { x: { pull_request_pending: false, pull_request_url: "hi"}}));
    spyPropertyGetter(mockConfigStore, 'releaseSubmitInFlight$').and.returnValue(cold('x', { x: false} ));
    spyPropertyGetter(mockConfigStore, 'countChangesInRelease$').and.returnValue(cold('x', { x: 2} ));
    spyPropertyGetter(mockConfigStore, 'release$').and.returnValue(cold('x', {x: {}}));
    spyPropertyGetter(mockConfigStore, 'sortedConfigs$').and.returnValue(cold('x', {x: {}}));
    spyPropertyGetter(mockConfigStore, 'releaseHistory$').and.returnValue(cold('x', {x: {}}));
    spyPropertyGetter(mockConfigStore, 'isAnyFilterPresent$').and.returnValue(cold('x|', {x: false}));
    spyPropertyGetter(mockConfigStore, 'importers$').and.returnValue(cold('x', {x: {'config_importers': []}}));
    spyPropertyGetter(mockConfigStore, 'serviceFilterConfig$').and.returnValue(cold('x', {x: {}}));
  });

  it('should create the config manager', () => {
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    const component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should add to search', () => {
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    const component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    const search = fixture.debugElement.query(By.css('re-search'));
    search.triggerEventHandler('searchTermChange', 'test');
    expect(mockRouter.navigate).toHaveBeenCalledOnceWith(
      ['test'], 
      { queryParams: { search: 'test'}, queryParamsHandling: 'merge'}
    );
  });

  it('should click checkbox', () => {
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    const component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    mockEditorService.getLatestFilters.and.returnValue(['general|my_edits', 'general|unreleased']);
    const checkboxes = fixture.debugElement.query(By.css('re-checkbox-filters'));
    checkboxes.triggerEventHandler('selectedCheckbox', { name:'general|my_edits', checked: true });
    expect(mockRouter.navigate).toHaveBeenCalledOnceWith(
      ['test'], 
      { queryParams: { filter: ['general|my_edits', 'general|unreleased']}, queryParamsHandling: 'merge'}
    );
  });

  it('should make changes to release', () => {
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    const component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    component.addToRelease("test_config");
    expect(mockConfigStore.addConfigToRelease).toHaveBeenCalledOnceWith("test_config");
    expect(mockConfigStore.incrementChangesInRelease).toHaveBeenCalledOnceWith();
  })

  it('editing features enabled by default', () => {
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    fixture.detectChanges();
    const pasteButton = fixture.debugElement.queryAll(By.css('.add-button'))[1];
    expect(pasteButton).toBeTruthy();
    expect(pasteButton.nativeElement.textContent).toEqual("content_paste");
  })

  it('importers correctly loaded: more than one', async() => {
    spyPropertyGetter(mockConfigStore, 'importers$').and.returnValue(cold('x', {x: {config_importers: [{importer_name: "test1"}, {importer_name: "test2"}]}}));
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    let loader = TestbedHarnessEnvironment.loader(fixture);
    fixture.detectChanges();
    getTestScheduler().flush();
    let menu = await loader.getHarness(MatMenuHarness);
    await menu.open();
    let items= await menu.getItems();
    expect(items.length).toEqual(2);
    let item1 = await items[0].getText();
    let item2 = await items[1].getText();
    expect(item1).toEqual("Test1 Importer");
    expect(item2).toEqual("Test2 Importer");
  })

  it('importers correctly loaded: one importer', async() => {
    spyPropertyGetter(mockConfigStore, 'importers$').and.returnValue(cold('x', {x: {config_importers: [{importer_name: "test1"}]}}));
    const fixture = TestBed.createComponent(ConfigManagerComponent);
    const component = fixture.debugElement.componentInstance;
    const importSpy = spyOn(component, 'onClickImport');
    let loader = TestbedHarnessEnvironment.loader(fixture);
    fixture.detectChanges();
    getTestScheduler().flush();
    const importButton = await loader.getHarness(MatButtonHarness.with({text: 'file_upload'}));
    await importButton.click();
    expect(importSpy).toHaveBeenCalledOnceWith(0);
  })


});