import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { copyHiddenTestCaseFields, TestCaseWrapper } from '@app/model/test-case';
import { Type } from '@app/model/config-model';
import { FormlyFieldConfig } from '@ngx-formly/core';
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

@Component({
  selector: 're-test-case-editor',
  templateUrl: './test-case-editor.component.html',
  styleUrls: ['./test-case-editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestCaseEditorComponent implements OnInit, OnDestroy {
  ngUnsubscribe = new Subject();
  editedTestCase$: Observable<TestCaseWrapper>;
  field: FormlyFieldConfig;
  options: any;

  testCaseWrapper: TestCaseWrapper;
  testCase: any = {};

  testStoreService: TestStoreService;
  form: FormGroup = new FormGroup({});
  private markHistoryChange = false;
  constructor(
    private appService: AppService,
    private editorService: EditorService,
    private dialog: MatDialog,
    private router: Router,
    private activeRoute: ActivatedRoute
  ) {
    this.editedTestCase$ = editorService.configStore.editedTestCase$;
    this.testStoreService = editorService.configStore.testService;
  }

  ngOnInit() {
    if (this.editorService.metaDataMap.testing.testCaseEnabled) {
      const subschema = cloneDeep(this.editorService.testSpecificationSchema);
      const schema = cloneDeep(this.appService.testCaseSchema);
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
        if (
          testCaseWrapper &&
          !this.editorService.configSchema.areTestCasesEqual(testCaseWrapper.testCase, this.testCase)
        ) {
          this.testCase = cloneDeep(this.testCaseWrapper.testCase);
        }
      });
    }
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(() => {
      if (this.form.valid && !this.markHistoryChange) {
        this.testStoreService.updateEditedTestCaseAndHistory(this.getFormTestCaseWrapper());
      }
      this.markHistoryChange = false;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSubmitTestCase() {
    const dialogRef = this.dialog.open(SubmitDialogComponent, {
      data: {
        name: this.testCase.test_case_name,
        type: Type.TESTCASE_TYPE,
        validate: () => this.testStoreService.validateEditedTestCase(),
        submit: () => this.testStoreService.submitEditedTestCase(),
      },
      disableClose: true,
    });
    dialogRef.afterClosed().subscribe(success => {
      if (success) {
        this.router.navigate([], {
          relativeTo: this.activeRoute,
          queryParams: {
            testCaseName: this.testCase.test_case_name,
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
    this.testStoreService.runEditedTestCase();
  }

  undoTestCase() {
    this.testStoreService.undoTestCase();
    this.setMarkHistoryChange();
  }

  redoTestCase() {
    this.testStoreService.redoTestCase();
    this.setMarkHistoryChange();
  }

  onCancelEditing() {
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { testCaseName: null, newTestCase: null, cloneTestCase: null, pasteTestCase: null },
      queryParamsHandling: 'merge',
    });
  }

  onPasteTestCase() {
    this.editorService.configStore.testService.setEditedPastedTestCase();
  }

  onCopyTestCase() {
    this.editorService.configStore.clipboardService.copyFromClipboard(this.testCase);
  }

  setMarkHistoryChange() {
    //Note: workaround as rawjson triggers an old form change on undo/redo
    this.markHistoryChange = true;
    this.form.updateValueAndValidity();
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
