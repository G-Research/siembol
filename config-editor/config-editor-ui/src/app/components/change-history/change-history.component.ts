import { FileHistory } from '../../model/config-model';
import { PopoverContent, PopoverRef } from '../../popover/popover-ref';

import { Component, OnInit } from '@angular/core';

@Component({
  selector: 're-change-history',
  templateUrl: './change-history.component.html',
  styleUrls: ['./change-history.component.scss'],
})
export class ChangeHistoryComponent implements OnInit {
    history: FileHistory[];
    content: PopoverContent;

  constructor(private popoverRef: PopoverRef) {
      this.history = this.popoverRef.data;
   }

  ngOnInit() {
      this.content = this.popoverRef.content;
  }

  close() {
      this.popoverRef.close({id: 1});
  }
}
