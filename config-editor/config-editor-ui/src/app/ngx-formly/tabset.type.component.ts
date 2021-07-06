import { Component, OnInit } from '@angular/core';
import { FieldType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-tabset-type',
  template: `
    <mat-tab-group
      class="tabset"
      animationDuration="0ms"
      (selectedTabChange)="onTabChange()"
      [(selectedIndex)]="selectedTab"
    >
      <mat-tab *ngFor="let tab of field.fieldGroup" [label]="tab?.templateOptions?.label">
        <formly-field [field]="tab"></formly-field>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [
    `
      ::ng-deep .tabset .mat-tab-body-wrapper {
        top: 8px !important;
      }
    `,
  ],
})
export class TabsetTypeComponent extends FieldType implements OnInit {
  defaultOptions = {
    defaultValue: {},
  };
  selectedTab = 0;

  ngOnInit() {
    this.selectedTab = this.field.templateOptions.tabIndex;
  }

  onTabChange() {
    this.field.templateOptions.tabIndex = this.selectedTab;
  }
}
