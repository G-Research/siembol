import { AfterViewInit, ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Config } from '@app/model';
import { CONFIG_TAB, TESTING_TAB, TEST_CASE_TAB } from '@app/model/test-case';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { JSONSchema7 } from 'json-schema';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EditorComponent } from '../editor/editor.component';
import { TestingType } from '@app/model/config-model';
import { SchemaService } from '@app/services/schema/schema.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-editor-view',
  styleUrls: ['./editor-view.component.scss'],
  templateUrl: './editor-view.component.html',
})
export class EditorViewComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(EditorComponent) editorComponent: EditorComponent;

  readonly TEST_CASE_TAB = TEST_CASE_TAB;
  readonly TESTING_TAB = TESTING_TAB;
  readonly CONFIG_TAB = CONFIG_TAB;
  readonly NO_TAB = -1;
  ngUnsubscribe = new Subject();
  configData: any;
  serviceName: string;
  schema: JSONSchema7;
  selectedTab = this.CONFIG_TAB.index;
  previousTab = this.NO_TAB;
  testingType = TestingType.CONFIG_TESTING;
  fields: FormlyFieldConfig[] = [];
  editedConfig$: Observable<Config>;

  constructor(
    private formlyJsonschema: FormlyJsonschema,
    private editorService: EditorService,
    private router: Router,
    private activeRoute: ActivatedRoute
  ) {
    this.serviceName = editorService.serviceName;
    this.schema = editorService.configSchema.schema;
    this.editedConfig$ = editorService.configStore.editedConfig$;
    this.fields = [
      this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), { map: SchemaService.renameDescription }),
    ];
  }

  testCaseEnabled: () => boolean = () => false;
  testingEnabled: () => boolean = () => false;

  ngOnInit() {
    this.editorService.configStore.editingTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(e => {
      if (e) {
        this.selectedTab = TEST_CASE_TAB.index;
      }
      if (this.previousTab === this.NO_TAB) {
        this.previousTab = this.selectedTab;
      }
    });
  }

  ngAfterViewInit() {
    this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((config: Config) => {
      this.fields = [
        this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), { map: SchemaService.renameDescription }),
      ];

      this.testingEnabled = () =>
        this.editorService.metaDataMap.testing.perConfigTestEnabled && this.editorComponent.form.valid;

      this.testCaseEnabled = () =>
        this.editorService.metaDataMap.testing.testCaseEnabled && this.editorComponent.form.valid && !config.isNew;

      this.configData = config.configData;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onTabChange() {
    if (this.previousTab === TEST_CASE_TAB.index) {
      this.router.navigate([], {
        relativeTo: this.activeRoute,
        queryParams: { testCaseName: null },
        queryParamsHandling: 'merge',
      });
    } else if (this.previousTab === CONFIG_TAB.index) {
      this.editorComponent.updateConfigInStore();
    }
    this.previousTab = this.selectedTab;
  }

  changeRoute() {
    this.router.navigate([this.serviceName]);
  }
}
