import { ChangeDetectionStrategy, Component, ViewChild, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { copyHiddenTestCaseFields, TestCaseResult, TestCaseWrapper } from '@app/model/test-case';
import { Type } from '@app/model/config-model';
import { FormlyForm, FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { EditorService } from '@app/services/editor.service';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TestStoreService } from '@app/services/store/test-store.service';
import { MatDialog } from '@angular/material/dialog';
import { SubmitDialogComponent } from '../../submit-dialog/submit-dialog.component';
import { ActivatedRoute, Router } from '@angular/router';
import { AppService } from '@app/services/app.service';
import { SchemaService } from '@app/services/schema/schema.service';
import { ConfigHistory } from '@app/model/store-state';
import { ClipboardService } from '@app/services/clipboard.service';

@Component({
  selector: 're-test-case-editor',
  templateUrl: './test-case-editor.component.html',
  styleUrls: ['./test-case-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestCaseEditorComponent implements OnInit, OnDestroy {
  public ngUnsubscribe = new Subject();
  public editedTestCase$: Observable<TestCaseWrapper>;
  public fields: FormlyFieldConfig[] = [];
  public options: any;

  public testCaseWrapper: TestCaseWrapper;
  public testCase: any;

  private testStoreService: TestStoreService;

  @ViewChild('formly', { static: true }) formly: FormlyForm;
  public form: FormGroup = new FormGroup({});
  public history: ConfigHistory = { past: [], future: [] };
  constructor(
    private appService: AppService,
    private editorService: EditorService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef,
    private router: Router,
    private activeRoute: ActivatedRoute,
    private clipboardService: ClipboardService
  ) {
    this.editedTestCase$ = editorService.configStore.editedTestCase$;
    this.testStoreService = editorService.configStore.testService;
  }

  ngOnInit() {
    if (this.editorService.metaDataMap.testing.testCaseEnabled) {
      const subschema = cloneDeep(this.editorService.testSpecificationSchema);
      let schema = cloneDeep(this.appService.testCaseSchema);
      schema.properties.test_specification = subschema;
      const schemaConverter = new FormlyJsonschema();
      this.editorService.configSchema.formatTitlesInSchema(schema, '');
      this.options = {
        resetOnHide: false,
        map: SchemaService.renameDescription,
      };
      this.fields = [schemaConverter.toFieldConfig(schema, this.options)];

      this.editedTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(testCaseWrapper => {
        this.testCaseWrapper = testCaseWrapper;
        this.testCase = testCaseWrapper !== null ? cloneDeep(this.testCaseWrapper.testCase) : {};

        this.options.formState = {
          mainModel: cloneDeep(this.testCase),
          rawObjects: {},
        };

        this.cd.detectChanges();
      });
    }

    this.history.past.splice(0, 0, {
      formState: cloneDeep(this.form.value),
    });
    this.form.valueChanges.subscribe(values => {
      if (
        this.form.valid &&
        (this.history.past.length == 0 || JSON.stringify(this.history.past[0].formState) !== JSON.stringify(values))
      ) {
        this.history.past.splice(0, 0, {
          formState: cloneDeep(values),
        });
        this.history.future = [];
      }
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSubmitTestCase() {
    const currentTestCase = this.getFormTestCaseWrapper();
    this.testStoreService.updateEditedTestCase(currentTestCase);
    const dialogRef = this.dialog.open(SubmitDialogComponent, {
      data: {
        name: currentTestCase.testCase.test_case_name,
        type: Type.TESTCASE_TYPE,
        validate: () => this.editorService.configStore.testService.validateEditedTestCase(),
        submit: () => this.editorService.configStore.testService.submitEditedTestCase(),
      },
      disableClose: true,
    });
    dialogRef.afterClosed().subscribe(success => {
      if (success) {
        this.router.navigate([], {
          relativeTo: this.activeRoute,
          queryParams: {
            testCaseName: currentTestCase.testCase.test_case_name,
            newTestCase: null,
            cloneTestCase: null,
            pasteTestCase: null,
          },
          queryParamsHandling: 'merge',
        });
      }
    });
  }

  onRunTestCase() {
    this.testStoreService.updateEditedTestCase(this.getFormTestCaseWrapper());
    this.testStoreService.runEditedTestCase();
  }

  undoTestCase() {
    // this.inUndoRedo = true;
    this.history.future.splice(0, 0, {
      formState: cloneDeep(this.history.past[0].formState),
    });
    this.history.past.shift();
    this.testStoreService.updateEditedTestCase(this.getTestCaseWrapper(this.history.past[0].formState));
    // this.updateAndWrapConfigData(this.config.configData);
  }

  redoTestCase() {
    // this.inUndoRedo = true;
    this.testStoreService.updateEditedTestCase(this.getTestCaseWrapper(this.history.future[0].formState));
    // this.updateAndWrapConfigData(this.config.configData);
    this.history.past.splice(0, 0, {
      formState: cloneDeep(this.history.future[0].formState),
    });
    this.history.future.shift();
  }

  onCancelEditing() {
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { testCaseName: null, newTestCase: null, cloneTestCase: null, pasteTestCase: null },
      queryParamsHandling: 'merge',
    });
  }

  async onPasteTestCase() {
    const valid = await this.clipboardService.validateTestCase();
    valid.subscribe(() => {
      this.editorService.configStore.testService.setEditedPastedTestCase();
    });
  }

  onCopyTestCase() {
    this.clipboardService.copy(this.testCase.testCase);
  }

  private getFormTestCaseWrapper(): TestCaseWrapper {
    return this.getTestCaseWrapper(this.form.value);
  }
  private getTestCaseWrapper(testCase: any): TestCaseWrapper {
    const ret = cloneDeep(this.testCaseWrapper) as TestCaseWrapper;
    ret.testCase = copyHiddenTestCaseFields(cloneDeep(testCase), this.testCase);
    // clean removes the undefined as well
    // ret.testCase = this.editorService.configSchema.cleanRawObjects(
    //   ret.testCase,
    //   this.formly.options.formState.rawObjects
    // );
    return ret;
  }
}
