import { ChangeDetectionStrategy, ChangeDetectorRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Component } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { Router, ActivatedRoute } from '@angular/router';
import { ConfigData, Config } from '@app/model';
import { CONFIG_TAB, TESTING_TAB, TEST_CASE_TAB } from '@app/model/test-case';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { JSONSchema7 } from 'json-schema';
import { cloneDeep } from 'lodash';
import * as omitEmpty from 'omit-empty';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EditorComponent } from '../editor/editor.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-editor-view',
  styleUrls: ['./editor-view.component.scss'],
  templateUrl: './editor-view.component.html'
})
export class EditorViewComponent implements OnInit, OnDestroy {
  @ViewChild(EditorComponent, { static: false }) editorComponent: EditorComponent;

  readonly TEST_CASE_TAB = TEST_CASE_TAB;
  readonly TESTING_TAB = TESTING_TAB;
  readonly CONFIG_TAB = CONFIG_TAB;
  readonly NO_TAB = -1;
  ngUnsubscribe = new Subject();

  testCaseEnabled: () => boolean = () => false;
  testingEnabled: () => boolean = () => false;
  configData: any;
  serviceName: string;
  schema: JSONSchema7;
  selectedTab = this.NO_TAB;
  previousTab = this.NO_TAB;

  fields: FormlyFieldConfig[] = [];
  formlyOptions: any;

  editedConfig$: Observable<Config>;

  constructor(
    private formlyJsonschema: FormlyJsonschema,
    private editorService: EditorService,
    private router: Router,
    private activeRoute: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {
    this.serviceName = editorService.serviceName;
    this.schema = editorService.configSchema.schema;
    this.editedConfig$ = editorService.configStore.editedConfig$;
    this.formlyOptions = {
      autoClear: true,
      map: editorService.configSchema.mapSchemaForm
    }
    this.fields = [
      this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), this.formlyOptions),
    ];
  }

  ngOnInit() {
    this.editorService.configStore.editingTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(e => {
      if (e) {
        this.selectedTab = TEST_CASE_TAB.index;
        if (this.previousTab === this.NO_TAB) {
          this.previousTab = this.selectedTab;
        }
      }
    });
  }

  public ngAfterViewInit() {
    this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((config: Config) => {
      this.fields = [
        this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), this.formlyOptions),
      ];

      this.testingEnabled = () => this.editorService.metaDataMap.testing.perConfigTestEnabled
        && this.editorComponent.form.valid;


      this.testCaseEnabled = () => this.editorService.metaDataMap.testing.testCaseEnabled
        && this.editorComponent.form.valid
        && !config.isNew;

      this.configData = config.configData;

      this.cd.markForCheck();
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onTabChange() {
    if (this.previousTab === TEST_CASE_TAB.index) {
      this.router.navigate(
      [],
      {
          relativeTo: this.activeRoute,
          queryParams: { testCaseName: null },
          queryParamsHandling: 'merge',
      });
    } else if (this.previousTab === CONFIG_TAB.index) {
      this.editorComponent.updateConfigInStore()
    }
    this.previousTab = this.selectedTab;
  }

  changeRoute() {
    this.router.navigate([this.serviceName]);
  }

}
