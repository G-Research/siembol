import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestCase, TestState } from '@app/model/test-case';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';

@Component({
    selector: 're-test-case',
    templateUrl: './test-case.component.html',
    styleUrls: ['./test-case.component.scss'],
    changeDetection: ChangeDetectionStrategy.Default,
})

export class TestCaseComponent {

    @Input() fields: FormlyFieldConfig[] = [];
    @Input() testCase: any = {};
    @Input() output;
    @Input() options: FormlyFormOptions = {};

    @Output() submittedTest = new EventEmitter<TestCase>();
    @Output() cancel = new EventEmitter<boolean>();
    @Output() runTest = new EventEmitter<TestCase>();

    public form: FormGroup = new FormGroup({});
    public testState = TestState;
}
