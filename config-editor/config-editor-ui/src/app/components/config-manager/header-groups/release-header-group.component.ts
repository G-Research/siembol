import { IHeaderGroupAngularComp } from '@ag-grid-community/angular';
import { IHeaderGroupParams } from '@ag-grid-community/core';
import { Component } from '@angular/core';
@Component({
  selector: 're-custom-header-group',
  template: `
  <div class="ag-header-group-cell-label">
    <div class="customHeaderLabel">
      {{ this.params.displayName }}: {{ this.params.context.componentParent.release.configs.length}} items
    </div>
    <div class="history-button"
      [popper]="fileHistory"
      [popperTrigger]="'hover'"
      [popperPlacement]="'left-start'"
      [popperHideOnScroll]="true"
      [popperDisableStyle]="true"
      [popperAppendTo]="'body'"
      >
      <popper-content #fileHistory>
          <re-change-history [history]="this.params.context.componentParent.releaseHistory.fileHistory"></re-change-history>
      </popper-content>
      <mat-icon>history</mat-icon>
      </div>
  </div>`,
  styles : [`
  .history-button {
    margin: 0 10px 0 10px;
    color: #868686;
    margin-top: 3px;
  }
  `],
})
export class ReleaseHeaderGroupComponent implements IHeaderGroupAngularComp {
  params!: IHeaderGroupParams;
  
  agInit(params: IHeaderGroupParams): void {
    this.params = params;
  }
}