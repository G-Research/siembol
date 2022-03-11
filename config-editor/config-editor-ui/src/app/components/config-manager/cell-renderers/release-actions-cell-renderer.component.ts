import { Component, OnInit } from "@angular/core";
import { ConfigStatus } from "@app/model/config-model";
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { ICellRendererParams } from '@ag-grid-community/core';

@Component({
  selector: "re-release-actions-cell-renderer",
  styleUrls: ['./release-actions-cell-renderer.component.scss'],
  template: `
  <span class="buttons">
    <a *ngIf="status === configStatusEnum.UP_TO_DATE">Up-to-date</a>
    <a *ngIf="status === configStatusEnum.UNRELEASED" mat-raised-button color="accent" (click)="addConfigToRelease()">Add to Release</a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" 
        mat-raised-button 
        color="accent" 
        (click)="upgradeConfig()">
        Upgrade v{{releasedVersion}} to v{{lastVersion}}
    </a>
    <a *ngIf="status === configStatusEnum.UPGRADABLE" mat-raised-button (click)="viewDiff()">View Diff</a>
    <a *ngIf="status !== configStatusEnum.UNRELEASED" (click)="removeConfigFromRelease()" [title]="'Remove Config From Release'">
        <mat-icon class="delete-button">clear</mat-icon>
    </a>
  </span>
  `,
})
export class ReleaseActionsCellRendererComponent implements ICellRendererAngularComp, OnInit {
  status: ConfigStatus;
  configStatusEnum = ConfigStatus;
  releasedVersion: number;
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

  removeConfigFromRelease() {
    this.params.context.componentParent.onRemove(this.params.node.rowIndex);
  }

  upgradeConfig() {
    this.params.context.componentParent.upgrade(this.params.node.rowIndex);
  }

  addConfigToRelease() {
    this.params.context.componentParent.addToRelease(this.params.node.rowIndex);
  }

  viewDiff() {
    this.params.context.componentParent.onView(this.params.node.rowIndex);
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    this.updateStatus();
    return true
  }

  private getStatus() {
    this.releasedVersion = this.params.node.data.releasedVersion;
    this.lastVersion = this.params.node.data.version;
    switch(this.releasedVersion) {
      case(0):
        return ConfigStatus.UNRELEASED;
      case(this.lastVersion):
        return ConfigStatus.UP_TO_DATE;
      default:
        return ConfigStatus.UPGRADABLE;
    }
  }
}
