import { Component, Input } from '@angular/core';
import { TestCaseResult, TestCaseWrapper } from '../../../model/test-case';

@Component({
  selector: 're-test-status-badge',
  templateUrl: './test-status-badge.component.html',
  styleUrls: ['./test-status-badge.component.scss'],
})
export class TestStatusBadgeComponent {
  @Input() testCaseWrapper: TestCaseWrapper;

  getTestBadge(testCaseResult: TestCaseResult): string {
    if (!testCaseResult) {
      return 'test-default';
    }

    if (testCaseResult.isRunning) {
      return 'test-running';
    }

    return !testCaseResult.evaluationResult
      ? 'test-skipped'
      : testCaseResult.evaluationResult.number_failed_assertions > 0
      ? 'test-fail'
      : testCaseResult.evaluationResult.number_skipped_assertions > 0
      ? 'test-skipped'
      : 'test-success';
  }
}
