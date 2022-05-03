import { ChangeDetectorRef, Component, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { CONFIG_TAB, TEST_CASE_TAB } from '@app/model/test-case';
import { EditorService } from '@app/services/editor.service';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { of } from 'rxjs';
import { EditorComponent } from '../editor/editor.component';
import { EditorViewComponent } from './editor-view.component';

const MockEditorService = {
  serviceName: 'test',
  configSchema: { schema: {} },
  configStore: {
    editedConfig$: of({}),
    editingTestCase$: of(true),
    searchTerm$: of("test"),
    serviceFilters$: of([]),
  },
  metaDataMap: {
    testing: {
      perConfigTesting: true,
      testCaseEnabled: true,
    },
  },
};

@Component({
  selector: 're-generic-editor',
  template: '',
  providers: [
    {
      provide: EditorComponent,
      useClass: EditorStubComponent,
    },
  ],
})
class EditorStubComponent {
  form = { valid: true };
}

describe('EditorViewComponent', () => {
  const routerSpy = { navigate: jasmine.createSpy('navigate') };
  const formlySpy = { toFieldConfig: jasmine.createSpy('navigate') };
  let component: EditorViewComponent;
  let fixture: ComponentFixture<EditorViewComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EditorViewComponent, EditorStubComponent],
      providers: [
        { provide: EditorService, useValue: MockEditorService },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { params: { configName: 'test' } },
          },
        },
        { provide: ChangeDetectorRef, useValue: {} },
        { provide: FormlyJsonschema, useValue: formlySpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditorViewComponent);
    component = fixture.componentInstance;
    component.editorComponent = TestBed.createComponent(EditorStubComponent).componentInstance as EditorComponent;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should navigate to testCase`, fakeAsync(() => {
    expect(component.selectedTab).toBe(CONFIG_TAB.index);
    component.ngOnInit();
    tick();
    expect(component.selectedTab).toBe(TEST_CASE_TAB.index);
  }));
});
