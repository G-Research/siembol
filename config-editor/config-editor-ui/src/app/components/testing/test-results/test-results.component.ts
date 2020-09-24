import { Component, OnInit } from '@angular/core';
import { TestCaseResult } from '../../../model/test-case';
import { PopoverContent, PopoverRef } from '../../../popover/popover-ref';

@Component({
  selector: 're-test-results',
  templateUrl: './test-results.component.html',
  styleUrls: ['./test-results.component.scss'],
})
export class TestResultsComponent implements OnInit {
    testResult: TestCaseResult;
    content: PopoverContent;

  constructor(private popoverRef: PopoverRef) {
      this.testResult = popoverRef.data;
   }

  ngOnInit() {
      this.content = this.popoverRef.content;
  }

  close() {
      this.popoverRef.close({id: 1});
  }
}
