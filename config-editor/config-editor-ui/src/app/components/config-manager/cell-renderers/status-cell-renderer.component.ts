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
    <a *ngIf="status === configStatusEnum.UNDEPLOYED" mat-raised-button color="accent" (click)="deployConfig()">Deploy</a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" mat-raised-button color="accent" (click)="upgradeConfig()">Upgrade</a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" mat-raised-button (click)="viewDiff()">View Diff</a>
    <a *ngIf="status !== configStatusEnum.UNDEPLOYED" (click)="deleteConfigFromDeployment()" [title]="'Delete Config From Deployment'">
        <mat-icon>delete</mat-icon>
    </a>
  </span>
  `,
})
export class StatusCellRendererComponent implements ICellRendererAngularComp, OnInit, OnDestroy {
  status: ConfigStatus;
  configStatusEnum = ConfigStatus;
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

  deleteConfigFromDeployment() {
    this.params.context.componentParent.onRemove(this.params.node.rowIndex);
    this.updateStatus();
  }

  upgradeConfig() {
    this.params.context.componentParent.upgrade(this.params.node.rowIndex);
    this.updateStatus();
  }

  deployConfig() {
    this.params.context.componentParent.addToDeployment(this.params.node.rowIndex);
    this.updateStatus();
  }

  viewDiff() {
    this.params.context.componentParent.onView(this.params.node.rowIndex);
    this.updateStatus();
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
    const deployedVersion = this.params.node.data.deployedVersion;
    const lastVersion = this.params.node.data.version;
    switch(deployedVersion) {
      case(0):
        return ConfigStatus.UNDEPLOYED;
      case(lastVersion):
        return ConfigStatus.UP_TO_DATE;
      default:
        return ConfigStatus.UPGRADABLE;
    }
  }
}
