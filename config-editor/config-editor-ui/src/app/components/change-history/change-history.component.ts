import { FileHistory } from '../../model/config-model';
import { Component, Input } from '@angular/core';

@Component({
  selector: 're-change-history',
  templateUrl: './change-history.component.html',
  styleUrls: ['./change-history.component.scss'],
})
export class ChangeHistoryComponent{
    @Input() history: FileHistory[];
}
