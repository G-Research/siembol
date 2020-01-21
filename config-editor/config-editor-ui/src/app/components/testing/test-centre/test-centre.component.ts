import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AppConfigService } from '@app/config';
import { TestCase, TestCaseMap, TestCaseResultDefault, TestState } from '@app/model/test-case';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';
import { PopupService } from '@app/popup.service';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig } from '@ngx-formly/core/lib/core';
import * as fromStore from 'app/store';
import { cloneDeep } from 'lodash';
import { from, Observable, of, Subject, throwError } from 'rxjs';
import { catchError, delay, map, switchMap, take, takeUntil, tap } from 'rxjs/operators';
import { EditorService } from '../../../editor.service';
import {
    ConfigData,
    ConfigTestDto,
    ConfigTestResult,
    ConfigWrapper,
    EditorResult,
} from '../../../model/config-model';
import { TestCaseWrapper, TestCaseWrapperDefault } from '../../../model/test-case';
import { SubmitTestcaseDialogComponent } from '../submit-testcase-dialog/submit-testcase-dialog.component';

interface OutputDict {
    [testCaseName: string]: string;
}

@Component({
  selector: 're-test-centre',
  templateUrl: './test-centre.component.html',
  styleUrls: ['./test-centre.component.scss'],
})
export class TestCentreComponent implements OnInit, OnDestroy {
  env: string;
  service: any;
  EVENT_HELP: string;
  private ngUnsubscribe = new Subject();
  public fields: FormlyFieldConfig[] = [];
  public options: any = {};
  public testCase: TestCaseWrapper = new TestCaseWrapperDefault();
  public alert: string;
  public form: FormGroup = new FormGroup({});
  public addNewTest = false;
  public testState = TestState;
  public output: OutputDict = {};

  public isSubmitted = true;

  public selectedTest: number = undefined;

  private readonly NO_NAME_MESSAGE = 'A name must be provided';
  private readonly UNIQUE_NAME_MESSAGE = 'Testcase names must be unique';
  private readonly SPACE_IN_NAME_MESSAGE = 'Testcase names cannot contain spaces';

  @Input() testCases: TestCaseWrapper[] = [];
  @Input() testCaseMap: Observable<TestCaseMap>;
  @Input() serviceName: string;
  @Input() selectedConfigIndex$: Observable<number>;
  @Input() selectedConfig: ConfigWrapper<ConfigData>;
  @Input() user: string;

  constructor(private editorService: EditorService,
    public snackbar: PopupService,
    private store: Store<fromStore.State>,
    private appConfig: AppConfigService,
    private changeDetector: ChangeDetectorRef,
    private dialog: MatDialog) {}

  ngOnInit() {
    this.service = this.editorService.getLoader(this.serviceName);
    this.store.select(fromStore.getConfigTestingEvent).pipe(take(1)).subscribe(e => this.alert = e);
    this.store.select(fromStore.getTestCaseSchema).pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
        this.service.getTestSpecificationSchema().pipe(takeUntil(this.ngUnsubscribe)).subscribe(l => {
                this.options.formState = {
                    mainModel: this.testCase,
                };
                const subschema = new FormlyJsonschema().toFieldConfig(l);
                const schemaConverter = new FormlyJsonschema();
                schemaConverter.testSpec = subschema;
                this.fields = [schemaConverter.toFieldConfig(s, this.options)];
            })
        })
    this.selectedConfigIndex$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
        this.addNewTest = false;
        this.changeDetector.markForCheck();
    })

    this.EVENT_HELP = this.appConfig.getUiMetadata(this.serviceName).testing.helpMessage;
    // TODO: need to do change detection better
    this.testCaseMap.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => this.changeDetector.markForCheck());
  }

  ngOnDestroy() {
    this.store.dispatch(new fromStore.StoreConfigTestingEvent(this.alert));
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  addTestCase() {
    this.addNewTest = true;
    this.testCase = new TestCaseWrapperDefault();
    this.testCase.testCase.version = 0;
    this.testCase.testCase.author = this.user;
    this.testCase.testCase.config_name = this.selectedConfig.name;
    this.testCase.testCase.test_case_name = '';
    this.options.formState = {
        mainModel: this.testCase,
    };
    this.isSubmitted = false;
  }

  onSubmitTestCase($event: TestCase) {
    const index = this.testCases.findIndex(a => a.testCase.test_case_name === $event.test_case_name);
    if (index >= 0 && $event.version === 0) {
        this.snackbar.openNotification(this.UNIQUE_NAME_MESSAGE);

        return;
    }

    if ($event.test_case_name.includes(' ')) {
        this.snackbar.openNotification(this.SPACE_IN_NAME_MESSAGE);

        return;
    }

    if ($event === undefined || $event === '') {
        this.snackbar.openNotification(this.NO_NAME_MESSAGE);

        return;
    }

    this.dialog.open(SubmitTestcaseDialogComponent, {
        data: {
            ...$event,
        },
    }).afterClosed().subscribe((submittedData: TestCase) => {
        if (submittedData) {
            const testCaseWrapper: TestCaseWrapper = {
                testCase: $event,
                testState: TestState.NOT_RUN,
                testResult: new TestCaseResultDefault(),
            }
            if (index !== -1) {
                this.testCases[index] = testCaseWrapper;
                this.store.dispatch(new fromStore.SubmitTestCaseEdit(testCaseWrapper));
            } else {
                this.testCases.push(cloneDeep(testCaseWrapper));
                this.store.dispatch(new fromStore.SubmitNewTestCase(testCaseWrapper));
            }
            this.addNewTest = false;
            this.testCase = new TestCaseWrapperDefault();
            this.isSubmitted = true;
        }
    });
  }

  editTestCase(index: number) {
      this.selectedTest = index;
      this.testCase = this.testCases[index];
      this.addNewTest = true;
      this.options.formState = {
        mainModel: this.testCase,
      };
      this.isSubmitted = true;
  }

  onCloneTestCase(testCase: TestCase) {
    this.addNewTest = true;
    this.testCase = cloneDeep(testCase);
    this.testCase.testCase.version = 0;
    this.testCase.testCase.author = this.user;
    this.testCase.testCase.config_name = this.selectedConfig.name;
    this.testCase.testCase.test_case_name += '_clone';
    this.options.formState = {
        mainModel: this.testCase,
    };
    this.isSubmitted = false;
  }

  onRunTestSuite() {
    this.store.dispatch(
        new fromStore.UpdateAllTestCaseState(this.selectedConfig.name, TestState.RUNNING, new TestCaseResultDefault()));
    from(this.testCases).pipe(
        tap(t => {
            this.runTestCase(t.testCase);
        }),
        delay(500)
    ).subscribe();
  }

  onRunTestCase(index: number) {
      this.store.dispatch(new fromStore.UpdateTestCaseState(
          this.testCases[index].testCase, TestState.RUNNING, new TestCaseResultDefault()));
      this.runTestCase(cloneDeep(this.testCases[index].testCase));
  }

  replacer(key, value) {
    return value === null ? undefined : value;
  }

  runTestCase(testcase: TestCase) {
    this.testSingleConfig(testcase).pipe(
          switchMap(s => {
            if (s === null ) {
                return of(null)
            }

            return this.editorService.evaluateTestCase(testcase, s).pipe(
                map(m => m.test_case_result),
                catchError(err => {
                    this.snackbar.openSnackBar(err, `Error occured while evaluating test cases`);
                    this.store.dispatch(
                        new fromStore.UpdateTestCaseState(testcase, TestState.ERROR, new TestCaseResultDefault()));

                    return throwError(err);
                }),
                tap(r => {
                    this.store.dispatch(
                        new fromStore.UpdateTestCaseState(testcase, r.number_failed_assertions > 0 ? TestState.FAIL : TestState.PASS, r));
                })
            )
          })
        ).subscribe(
            (res: EditorResult<ConfigTestResult>) => {},
            err => console.error('HTTP error', err)
        )
  }

  private testSingleConfig(testcase: TestCase): Observable<any> {
    const testDto: ConfigTestDto = {
        files: [{
            content: this.selectedConfig.configData,
        }],
        event: JSON.parse(JSON.stringify(testcase.test_specification, this.replacer)),
    }

    return this.editorService.getLoader(this.serviceName).testSingleConfig(testDto).pipe(
        map(b => {
            if (b.status_code === 'OK') {
                this.output[testcase.test_case_name] =
                    b.attributes.test_result_output + '\n' + JSON.stringify(b.attributes.test_result_raw_output);

                return b.attributes.test_result_raw_output;
            } else {
                if (b.attributes.message && b.attributes.exception) {
                  this.output[testcase.test_case_name] = b.attributes.message + '\n' + b.attributes.exception;
                } else if (b.attributes.message !== undefined) {
                  this.output[testcase.test_case_name] = b.attributes.message;
                } else if (b.attributes.exception !== undefined) {
                  this.output[testcase.test_case_name] = b.attributes.exception;
                }
                this.store.dispatch(
                    new fromStore.UpdateTestCaseState(testcase, TestState.ERROR, new TestCaseResultDefault()));
            }

            return null;
        }),
        catchError(err => {
          this.snackbar.openSnackBar(err, `Error could not contact ${this.env} backend`);

          return throwError(err);
        })
    )
  }

  runSingleTest(testcase: TestCase) {
    this.testSingleConfig(testcase).subscribe(s => this.changeDetector.markForCheck(), err => console.error(err));
  }

  onCancel() {
    this.addNewTest = false;
    this.testCase = new TestCaseWrapperDefault();
  }

  setTestBadge(testCase: TestCaseWrapper): string {
    if (testCase === undefined) {
        return 'test-default';
    }

    switch (testCase.testState) {
        case TestState.PASS:
            return testCase.testResult.number_skipped_assertions > 0 ? 'test-skipped' : 'test-success';
        case TestState.FAIL: return 'test-fail';
        case TestState.ERROR: return 'test-error';
        case TestState.NOT_RUN: return 'test-default';
        case TestState.RUNNING: return 'test-running';
    }
  }
}
