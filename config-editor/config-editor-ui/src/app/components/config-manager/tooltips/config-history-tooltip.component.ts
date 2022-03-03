import { ITooltipAngularComp } from 'ag-grid-angular';
import { ITooltipParams } from 'ag-grid-community';
import { Component } from '@angular/core';
import { FileHistory } from '@app/model';

@Component({
  selector: 're-config-history-tooltip-component',
  template: `
  <div class="custom-tooltip">
    <re-change-history [history]="configHistory"></re-change-history>
  </div>
  `,
  styles: [],
})
export class ConfigHistoryTooltipComponent implements ITooltipAngularComp {
  configHistory: FileHistory[];
  private params: ITooltipParams;

  agInit(params: ITooltipParams): void {
    this.params = params;
    this.configHistory = this.params.node.data.configHistory;
  }
}
