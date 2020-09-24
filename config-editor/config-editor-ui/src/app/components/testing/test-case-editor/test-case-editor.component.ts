import { ChangeDetectionStrategy, Component, ViewChild, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestCaseWrapper } from '@app/model/test-case';
import { FormlyForm, FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { EditorService } from '../../../services/editor.service';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';
import { AppConfigService } from '../../../config/app-config.service';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TestStoreService } from '../../../services/test-store.service';
import { MatDialog } from '@angular/material/dialog';
import { SubmitTestcaseDialogComponent } from '../submit-testcase-dialog/submit-testcase-dialog.component';

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
    public options: any = { autoClear: false }

    public testCaseWrapper: TestCaseWrapper;
    public testCase: any;

    private testStoreService: TestStoreService;
    private testCaseCopy: any;
    
    @ViewChild('formly', { static: true }) formly: FormlyForm;
    public form: FormGroup = new FormGroup({});

    constructor(
        private appConfig: AppConfigService,
        private editorService: EditorService,
        private dialog: MatDialog,
        private cd: ChangeDetectorRef
    ) {
        this.editedTestCase$ = editorService.configStore.editedTestCase$;
        this.testStoreService = editorService.configStore.testService;
    }

    ngOnInit() {
        if (this.editorService.metaDataMap.testing.testCaseEnabled) {
            const subschema = new FormlyJsonschema().toFieldConfig(cloneDeep(this.editorService.testSpecificationSchema));
            const schemaConverter = new FormlyJsonschema();
            schemaConverter.testSpec = subschema;
            this.fields = [schemaConverter.toFieldConfig(cloneDeep(this.appConfig.getTestCaseSchema()), this.options)];

            this.editedTestCase$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(testCaseWrapper => {
                this.testCaseWrapper = testCaseWrapper;
                this.testCase = testCaseWrapper !== null ? cloneDeep(this.testCaseWrapper.testCase) : {};
                this.testCaseCopy = cloneDeep(this.testCase);

                this.form = new FormGroup({});
                this.options.formState = {
                    mainModel: this.testCase,
                    rawObjects: {},
                }
            });
        }
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    onSubmitTestCase() {
        const currentTestCase = this.getTestCaseWrapper();
        this.testStoreService.updateEditedTestCase(currentTestCase);
        this.dialog.open(SubmitTestcaseDialogComponent, {
            data: currentTestCase.testCase.test_case_name
        },
        );
    }

    onRunTestCase() {
        this.testStoreService.updateEditedTestCase(this.getTestCaseWrapper());
        this.testStoreService.runEditedTestCase();
    }

    onUpdateTestCase(event: any) {
        this.testCaseCopy = event;
    }

    private getTestCaseWrapper(): TestCaseWrapper {
        const ret = cloneDeep(this.testCaseWrapper) as TestCaseWrapper;
        ret.testCase = cloneDeep(this.testCaseCopy);
        ret.testCase.test_specification = this.editorService.configWrapper
            .cleanRawObjects(ret.testCase.test_specification, this.formly.options.formState.rawObjects);
        return ret;
    }
}
