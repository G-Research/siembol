import { Component, OnDestroy } from "@angular/core";
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { ICellRendererParams } from '@ag-grid-community/core';

@Component({
  selector: "re-action-cell-renderer",
  styleUrls: ['./action-cell-renderer.component.scss'],
  template: `
  <span class="buttons">
    <a (click)="editConfig()" [title]="'Edit Config'">
        <mat-icon>edit</mat-icon>
    </a>
    <a (click)="viewConfig()" [title]="'View Json'">
        <mat-icon>pageview</mat-icon>
    </a>
    <a (click)="cloneConfig()" [title]="'Clone Config'">
        <mat-icon>content_copy</mat-icon>
    </a>
    <a *ngIf="notDeployed" (click)="deleteConfigFromStore()" [title]="'Delete Config From Store'">
        <mat-icon>delete</mat-icon>
    </a>
  </span>
  `,
})
export class ActionCellRendererComponent implements ICellRendererAngularComp, OnDestroy {
  notDeployed: boolean;
  private params: any;

  agInit(params: any) {
    this.refresh(params);
  }

  editConfig() {
    this.params.context.componentParent.onEdit(this.params.node.rowIndex);
  }

  viewConfig() {
    this.params.context.componentParent.onView(this.params.node.rowIndex);
  }

  cloneConfig() {
    this.params.context.componentParent.onClone(this.params.node.rowIndex);
  }

  deleteConfigFromStore() {
    this.params.context.componentParent.onRemove(this.params.node.rowIndex);
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    this.notDeployed = this.params.node.data.deployedVersion === 0;
    return true
  }
}
