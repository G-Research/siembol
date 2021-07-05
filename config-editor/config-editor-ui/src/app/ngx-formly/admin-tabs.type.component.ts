import { Component, OnInit } from '@angular/core';
import { FieldType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-admintab-type',
  template: `
    <mat-tab-group
      class="admin-tabs"
      animationDuration="0ms"
      (selectedTabChange)="onTabChange()"
      [(selectedIndex)]="selectedTab"
    >
      <mat-tab [label]="'General Properties'">
        <ng-container *ngFor="let f of field.fieldGroup">
          <formly-field
            *ngIf="!tabTypes.includes(f.type) || f.templateOptions.genericSettingTab"
            [field]="f"
          ></formly-field>
        </ng-container>
      </mat-tab>
      <ng-container *ngFor="let tab of field.fieldGroup">
        <mat-tab
          *ngIf="tabTypes.includes(tab.type) && !tab.templateOptions.genericSettingTab"
          [label]="tab?.templateOptions?.label"
        >
          <formly-field [field]="tab"></formly-field>
        </mat-tab>
      </ng-container>
    </mat-tab-group>
  `,
  styles: [
    `
      ::ng-deep .admin-tabs .mat-tab-body-wrapper {
        top: 8px !important;
      }
    `,
  ],
})
export class AdminTabTypeComponent extends FieldType implements OnInit {
  selectedTab = 0;
  tabTypes = ['array', 'object', 'rawobject'];

  ngOnInit() {
    this.selectedTab = this.field.templateOptions.tabIndex;
  }

  onTabChange() {
    this.field.templateOptions.tabIndex = this.selectedTab;
  }
}
