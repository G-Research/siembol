import { Component, OnDestroy, OnInit } from "@angular/core";
import { ConfigStatus } from "@app/model/config-model";
import { ICellRendererAngularComp } from "ag-grid-angular";
import { ICellRendererParams } from "ag-grid-community";

@Component({
  selector: "re-status-cell-renderer",
  styleUrls: ['./status-cell-renderer.component.scss'],
  template: `
  <span class="buttons">
    <a *ngIf="status === configStatusEnum.UP_TO_DATE">Up-to-date</a>
    <a *ngIf="status === configStatusEnum.UNDEPLOYED" mat-raised-button color="accent" (click)="deployConfig()">Deploy Config</a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" 
        mat-raised-button 
        color="accent" 
        (click)="upgradeConfig()">
        Upgrade v{{deployedVersion}} to v{{lastVersion}}
    </a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" mat-raised-button (click)="viewDiff()">View Diff</a>
    <a *ngIf="status !== configStatusEnum.UNDEPLOYED" (click)="deleteConfigFromRelease()" [title]="'Delete Config From Release'">
        <mat-icon class="delete-button">delete</mat-icon>
    </a>
  </span>
  `,
})
export class StatusCellRendererComponent implements ICellRendererAngularComp, OnInit, OnDestroy {
  status: ConfigStatus;
  configStatusEnum = ConfigStatus;
  deployedVersion: number;
  lastVersion: number;
  private params: any;

  ngOnInit() {
    this.updateStatus();
  }

  updateStatus() { 
    this.status = this.getStatus();
  }

  agInit(params: any): void {
    this.params = params;
  }

  deleteConfigFromRelease() {
    this.params.context.componentParent.onRemove(this.params.node.rowIndex);
    this.params.context.componentParent.incrementChangesInRelease();
  }

  upgradeConfig() {
    this.params.context.componentParent.upgrade(this.params.node.rowIndex);
    this.params.context.componentParent.incrementChangesInRelease();
  }

  deployConfig() {
    this.params.context.componentParent.addToRelease(this.params.node.rowIndex);
    this.params.context.componentParent.incrementChangesInRelease();
  }

  viewDiff() {
    this.params.context.componentParent.onView(this.params.node.rowIndex);
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    this.updateStatus();
    return true
  }

  private getStatus() {
    this.deployedVersion = this.params.node.data.deployedVersion;
    this.lastVersion = this.params.node.data.version;
    switch(this.deployedVersion) {
      case(0):
        return ConfigStatus.UNDEPLOYED;
      case(this.lastVersion):
        return ConfigStatus.UP_TO_DATE;
      default:
        return ConfigStatus.UPGRADABLE;
    }
  }
}
