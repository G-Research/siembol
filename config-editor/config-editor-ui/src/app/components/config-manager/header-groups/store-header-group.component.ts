import { IHeaderGroupAngularComp } from '@ag-grid-community/angular';
import { IHeaderGroupParams } from '@ag-grid-community/core';
import { Component } from '@angular/core';
@Component({
  selector: 're-custom-header-group',
  template: `
  <div class="ag-header-group-cell-label">
    <div class="customHeaderLabel">
      {{ this.params.displayName }}: {{ this.params.context.componentParent.configs.length}} items
    </div>
  </div>`,
  styles : [],
})
export class StoreHeaderGroupComponent implements IHeaderGroupAngularComp {
  params!: IHeaderGroupParams;
  
  agInit(params: IHeaderGroupParams): void {
    this.params = params;
  }
}