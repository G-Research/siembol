import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';
import { ChangeDetectionStrategy, OnInit, Input, OnDestroy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { Component } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { ConfigData, ConfigWrapper } from '@app/model';
import { TEST_CASE_TAB_NAME, TESTING_TAB_NAME } from '@app/model/test-case';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { JSONSchema7 } from 'json-schema';
import { EditorService } from '@app/services/editor.service';
import { Router } from '@angular/router';
import { EditorComponent } from '../editor/editor.component';
import * as omitEmpty from 'omit-empty';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-editor-view',
  styleUrls: ['./editor-view.component.scss'],
  templateUrl: './editor-view.component.html'
})
export class EditorViewComponent implements OnInit, OnDestroy {
  @ViewChild(EditorComponent, { static: false }) editorComponent: EditorComponent;

  readonly TEST_CASE_TAB_NAME = TEST_CASE_TAB_NAME;
  readonly TESTING_TAB_NAME = TESTING_TAB_NAME;
  ngUnsubscribe = new Subject();

  testCaseEnabled: () => boolean = () => false;
  testingEnabled: () => boolean = () => false;
  configData: any;
  serviceName: string;
  schema: JSONSchema7;

  fields: FormlyFieldConfig[] = [];
  formlyOptions: any = { autoClear: true };

  onClickTestCase$: Subject<MatTabChangeEvent> = new Subject();
  editedConfig$: Observable<ConfigWrapper<ConfigData>>;
  constructor(
    private formlyJsonschema: FormlyJsonschema,
    private editorService: EditorService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {
    this.serviceName = editorService.serviceName;
    this.schema = editorService.configSchema;
    this.editedConfig$ = editorService.configStore.editedConfig$;
    this.fields = [
      this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), this.formlyOptions),
    ];
  }

  ngOnInit() {
  }

  public ngAfterViewInit() {
    this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((config: ConfigWrapper<ConfigData>) => {
      this.fields = [
        this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), this.formlyOptions),
      ];

      this.testingEnabled = () => this.editorService.metaDataMap.testing.perConfigTestEnabled
        && this.editorComponent.form.valid;


      this.testCaseEnabled = () => this.editorService.metaDataMap.testing.testCaseEnabled
        && this.editorComponent.form.valid
        && !config.isNew;

      this.configData = omitEmpty(this.editorService.configWrapper.unwrapConfig(config.configData));

      this.cd.markForCheck();
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  changeRoute() {
    this.router.navigate([this.serviceName]);
  }
}
