import { Component, Input } from '@angular/core';
import { TestCaseResult } from '../../../model/test-case';

@Component({
  selector: 're-test-results',
  templateUrl: './test-results.component.html',
  styleUrls: ['./test-results.component.scss'],
})
export class TestResultsComponent {
    @Input() testResult: TestCaseResult;
}
