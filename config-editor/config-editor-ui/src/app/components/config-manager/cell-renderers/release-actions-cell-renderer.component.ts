import { Component } from "@angular/core";
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
    <a *ngIf="status === configStatusEnum.UPGRADABLE" mat-raised-button color="primary" (click)="viewDiff()">View Diff</a>
    <a *ngIf="status !== configStatusEnum.UNRELEASED" (click)="removeConfigFromRelease()" [title]="'Remove Config From Release'">
        <mat-icon class="delete-button">clear</mat-icon>
    </a>
  </span>
  `,
})
export class ReleaseActionsCellRendererComponent implements ICellRendererAngularComp {
  status: ConfigStatus;
  configStatusEnum = ConfigStatus;
  releasedVersion: number;
  lastVersion: number;
  private params: any;

  agInit(params: any): void {
    this.params = params;
    this.updateValues();
  }

  removeConfigFromRelease() {
    this.params.context.componentParent.onRemove(this.params.node.id);
  }

  upgradeConfig() {
    this.params.context.componentParent.upgrade(this.params.node.id);
  }

  addConfigToRelease() {
    this.params.context.componentParent.addToRelease(this.params.node.id);
  }

  viewDiff() {
    this.params.context.componentParent.onViewDiff(this.params.node.id);
  }

  updateValues() {
    this.status = this.params.node.data.status;
    this.releasedVersion = this.params.node.data.releasedVersion;
    this.lastVersion = this.params.node.data.version;
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    this.updateValues();
    return true
  }

}
