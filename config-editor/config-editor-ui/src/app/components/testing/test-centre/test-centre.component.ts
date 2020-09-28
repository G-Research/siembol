import { Component, OnDestroy, OnInit } from '@angular/core';
import { PopupService } from '@app/popup.service';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EditorService } from '@services/editor.service';
import { TestCaseWrapper } from '@model/test-case';
import { TestStoreService } from '../../../services/test-store.service';
import { TestCaseResult } from '../../../model/test-case';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
    selector: 're-test-centre',
    templateUrl: './test-centre.component.html',
    styleUrls: ['./test-centre.component.scss'],
})
export class TestCentreComponent implements OnInit, OnDestroy {
    private testStoreService: TestStoreService;
    private ngUnsubscribe = new Subject();

    public testCases$: Observable<TestCaseWrapper[]>;
    public editingTestCase$: Observable<boolean>;
    public editedTestCase$: Observable<TestCaseWrapper>;

    public testCases: TestCaseWrapper[];
    public testCase: TestCaseWrapper;

    constructor(private editorService: EditorService,
        public snackbar: PopupService,
        private router: Router,
        private activeRoute: ActivatedRoute) {
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
        this.testStoreService.setEditedTestCaseNew();
    }

    onEditTestCase(index: number) {
        const name = this.testCases[index].testCase.test_case_name;
        this.router.navigate(
        [],
        {
            relativeTo: this.activeRoute,
            queryParams: { testCaseName: name },
            queryParamsHandling: 'merge',
        });
    }

    onCloneTestCase(index: number) {
        const name = this.testCases[index].testCase.test_case_name;
        this.testStoreService.setEditedClonedTestCaseByName(name);
    }

    onRunTestSuite() {
        this.testStoreService.runEditedConfigTestSuite();
    }

    onCancelEditing() {
        this.router.navigate(
        [],
        {
            relativeTo: this.activeRoute,
            queryParams: { testCaseName: null },
            queryParamsHandling: 'merge',
        });
    }

    getTestBadge(testCaseResult: TestCaseResult): string {
        if (!testCaseResult) {
            return 'test-default';
        }

        if (testCaseResult.isRunning) {
            return 'test-running'
        }

        return testCaseResult.evaluationResult.number_failed_assertions > 0
            ? 'test-fail' : testCaseResult.evaluationResult.number_skipped_assertions > 0
                ? 'test-skipped' : 'test-success';

    }
}
