import { Component } from "@angular/core";
import { FileHistory } from "@app/model/config-model";
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { ICellRendererParams } from '@ag-grid-community/core';

@Component({
  selector: "re-label-cell-renderer",
  styles: [`
    .custom-tooltip {
      overflow-y: auto;
      background-color: #fff;
      padding: 1rem;
      font-size: 14px;
      box-shadow: 0 20px 20px 2px rgba(0, 0, 0, 0.3);
      border-radius: 5px;
      max-height: 400px;
      color: black;
    }
    .column-fixed {
      display: inline-block;
      padding-right: 20px;
      max-width: 100%;
    }
  `,
  ],
  template: `
  <div class="column-fixed" 
    [popper]="fileHistory"
    [popperTrigger]="'hover'"
    [popperPlacement]="'left-start'"
    [popperHideOnScroll]="true"
    [popperDisableStyle]="true"
    [popperAppendTo]="'body'"
    [popperModifiers]="{preventOverflow: {boundariesElement: 'viewport'}}">
    <popper-content #fileHistory>
      <div class="custom-tooltip">
        <b>Config Name</b>: {{ configName }}<br>
        <b>Version</b>: {{ version }}<br>
        <b>Number of test cases</b>: {{ testCasesCount }}
        <re-change-history [history]="configHistory"></re-change-history>
      </div>
    </popper-content>
      {{ configName }}
  </div>
  `,
})
export class ConfigNameCellRendererComponent implements ICellRendererAngularComp {
  configHistory: FileHistory[];
  configName: string;
  version: number;
  testCasesCount: number;
  private params: any;

  agInit(params: any): void {
    this.params = params;
    this.configHistory = this.params.node.data.configHistory;
    this.version = this.params.node.data.version;
    this.testCasesCount = this.params.node.data.testCasesCount;
    this.configName = params.value;
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    return true
  }
}
