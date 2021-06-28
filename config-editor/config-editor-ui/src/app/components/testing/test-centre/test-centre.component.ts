import { Component, OnDestroy, OnInit } from '@angular/core';
import { PopupService } from '@app/services/popup.service';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EditorService } from '@services/editor.service';
import { TestCaseWrapper } from '@model/test-case';
import { TestStoreService } from '@app/services/store/test-store.service';
import { Router, ActivatedRoute } from '@angular/router';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { AppConfigService } from '@app/services/app-config.service';
import { Type } from '@app/model/config-model';

@Component({
  selector: 're-test-centre',
  templateUrl: './test-centre.component.html',
  styleUrls: ['./test-centre.component.scss'],
})
export class TestCentreComponent implements OnInit, OnDestroy {
  @BlockUI() blockUI: NgBlockUI;
  testCases$: Observable<TestCaseWrapper[]>;
  editingTestCase$: Observable<boolean>;
  editedTestCase$: Observable<TestCaseWrapper>;

  testCases: TestCaseWrapper[];
  testCase: TestCaseWrapper;

  private testStoreService: TestStoreService;
  private ngUnsubscribe = new Subject();
  constructor(
    private editorService: EditorService,
    public snackbar: PopupService,
    private router: Router,
    private activeRoute: ActivatedRoute,
    private configService: AppConfigService
  ) {
    this.testCases$ = this.editorService.configStore.editedConfigTestCases$;
    this.editingTestCase$ = this.editorService.configStore.editingTestCase$;
    this.testStoreService = this.editorService.configStore.testService;
    this.editedTestCase$ = this.editorService.configStore.editedTestCase$;
  }

  ngOnInit() {
    if (this.editorService.metaDataMap.testing.testCaseEnabled) {
      this.testCases$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(testCases => {
        this.testCases = testCases;
      });
      this.editedTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(testCaseWrapper => {
        this.testCase = testCaseWrapper;
      });
    }
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onAddTestCase() {
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { newTestCase: true },
      queryParamsHandling: 'merge',
    });
  }

  onEditTestCase(index: number) {
    const name = this.testCases[index].testCase.test_case_name;
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { testCaseName: name },
      queryParamsHandling: 'merge',
    });
  }

  onCloneTestCase(index: number) {
    const name = this.testCases[index].testCase.test_case_name;
    this.router.navigate([], {
      relativeTo: this.activeRoute,
      queryParams: { newTestCase: true, cloneTestCase: name },
      queryParamsHandling: 'merge',
    });
  }

  onRunTestSuite() {
    this.testStoreService.runEditedConfigTestSuite();
  }

  onDeleteTestCase(index: number) {
    this.blockUI.start('deleting test case');
    this.editorService.configStore
      .deleteTestCase(
        this.testCases[index].testCase.config_name,
        this.testCases[index].testCase.test_case_name
      )
      .subscribe(() => {
        this.blockUI.stop();
      });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  onPasteTestCaseNew() {
    this.editorService.configStore.clipboardService.validateConfig(Type.TESTCASE_TYPE).subscribe(() => {
      this.router.navigate([], {
        relativeTo: this.activeRoute,
        queryParams: { pasteTestCase: true },
        queryParamsHandling: 'merge',
      });
    });
  }
}
