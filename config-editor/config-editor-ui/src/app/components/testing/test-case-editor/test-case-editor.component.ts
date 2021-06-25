import { ChangeDetectionStrategy, Component, ViewChild, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { copyHiddenTestCaseFields, TestCaseWrapper } from '@app/model/test-case';
import { Type } from '@app/model/config-model';
import { FormlyForm, FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { EditorService } from '@app/services/editor.service';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { Observable, Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { TestStoreService } from '@app/services/store/test-store.service';
import { MatDialog } from '@angular/material/dialog';
import { SubmitDialogComponent } from '../../submit-dialog/submit-dialog.component';
import { ActivatedRoute, Router } from '@angular/router';
import { AppService } from '@app/services/app.service';
import { SchemaService } from '@app/services/schema/schema.service';
import { ConfigHistoryService } from '@app/services/config-history.service';

@Component({
  selector: 're-test-case-editor',
  templateUrl: './test-case-editor.component.html',
  styleUrls: ['./test-case-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ConfigHistoryService],
})
export class TestCaseEditorComponent implements OnInit, OnDestroy {
  public ngUnsubscribe = new Subject();
  public editedTestCase$: Observable<TestCaseWrapper>;
  public field: FormlyFieldConfig;
  public options: any;

  public testCaseWrapper: TestCaseWrapper;
  public testCase: any;

  private testStoreService: TestStoreService;
  private markHistoryChange = false;

  public form: FormGroup = new FormGroup({});
  constructor(
    private appService: AppService,
    private editorService: EditorService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef,
    private router: Router,
    private activeRoute: ActivatedRoute,
    public configHistoryService: ConfigHistoryService
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
      this.field = schemaConverter.toFieldConfig(schema, this.options);

      this.editedTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(testCaseWrapper => {
        this.testCaseWrapper = testCaseWrapper;
        this.testCase = testCaseWrapper !== null ? cloneDeep(this.testCaseWrapper.testCase) : {};
        this.cd.detectChanges();
      });
    }

    this.configHistoryService.addConfig(cloneDeep(this.form.value));
    this.form.valueChanges.pipe(debounceTime(200), takeUntil(this.ngUnsubscribe)).subscribe(values => {
      if (this.form.valid && !this.markHistoryChange) {
        this.configHistoryService.addConfig(cloneDeep(values));
      }
      this.markHistoryChange = false;
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
    this.markHistoryChange = true;
    let nextState = this.configHistoryService.undoConfig();
    this.testStoreService.updateEditedTestCase(this.getTestCaseWrapper(nextState.formState));
    this.form.updateValueAndValidity();
  }

  redoTestCase() {
    this.markHistoryChange = true;
    let nextState = this.configHistoryService.redoConfig();
    this.testStoreService.updateEditedTestCase(this.getTestCaseWrapper(nextState.formState));
    this.form.updateValueAndValidity();
  }

  onCancelEditing() {
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { testCaseName: null, newTestCase: null, cloneTestCase: null, pasteTestCase: null },
      queryParamsHandling: 'merge',
    });
  }

  onPasteTestCase() {
    this.editorService.clipboardService.validateTestCase().subscribe(() => {
      const pastedTest = this.editorService.configStore.testService.setEditedPastedTestCase();
      this.configHistoryService.addConfig(pastedTest);
    });
  }

  onCopyTestCase() {
    this.editorService.clipboardService.copy(this.testCase);
  }

  private getFormTestCaseWrapper(): TestCaseWrapper {
    return this.getTestCaseWrapper(this.form.value);
  }
  private getTestCaseWrapper(testCase: any): TestCaseWrapper {
    const ret = cloneDeep(this.testCaseWrapper) as TestCaseWrapper;
    ret.testCase = copyHiddenTestCaseFields(cloneDeep(testCase), this.testCase);
    return ret;
  }
}
