import { Component } from "@angular/core";
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { ICellRendererParams } from '@ag-grid-community/core';

@Component({
  selector: "re-store-action-cell-renderer",
  styleUrls: ['./store-action-cell-renderer.component.scss'],
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
    <a *ngIf="notReleased" (click)="deleteConfigFromStore()" [title]="'Delete Config From Store'">
        <mat-icon>delete</mat-icon>
    </a>
  </span>
  `,
})
export class StoreActionCellRendererComponent implements ICellRendererAngularComp {
  notReleased: boolean;
  private params: any;

  agInit(params: any) {
    this.refresh(params);
  }

  editConfig() {
    this.params.context.componentParent.onEdit(this.params.node.id);
  }

  viewConfig() {
    this.params.context.componentParent.onView(this.params.node.id);
  }

  cloneConfig() {
    this.params.context.componentParent.onClone(this.params.node.id);
  }

  deleteConfigFromStore() {
    this.params.context.componentParent.deleteConfigFromStore(this.params.node.id);
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    this.notReleased = this.params.node.data.releasedVersion === 0;
    return true
  }
}
