import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewChild,
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestCase, TestCaseWrapper, TestState } from '@app/model/test-case';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyForm } from '@ngx-formly/core';

@Component({
    selector: 're-test-case',
    templateUrl: './test-case.component.html',
    styleUrls: ['./test-case.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})

export class TestCaseComponent {

    @Input() fields: FormlyFieldConfig[] = [];
    @Input() testCase: TestCaseWrapper;
    @Input() output;
    @Input() options: FormlyFormOptions = {};

    @Output() submittedTest = new EventEmitter<TestCase>();
    @Output() cancel = new EventEmitter<boolean>();
    @Output() testConfig = new EventEmitter<TestCase>();
    @Output() runTestCase = new EventEmitter<TestCase>();

    @ViewChild('form', {static: false}) formly: FormlyForm;
    public form: FormGroup = new FormGroup({});
    public testState = TestState;

    outputTestCase: TestCase;

    updateOutput(event: TestCase) {
        this.outputTestCase = event;
    }
}
