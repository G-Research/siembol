import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-admintab-type',
  template: `
  <mat-tab-group animationDuration="0ms">
    <mat-tab [label]="'General Properties'">
        <ng-container *ngFor="let f of field.fieldGroup">
        <formly-field *ngIf="!tabTypes.includes(f.type) || f.templateOptions.genericSettingTab" [field]="f"></formly-field>
        </ng-container>
    </mat-tab>
    <ng-container *ngFor="let tab of field.fieldGroup">
    <mat-tab *ngIf="tabTypes.includes(tab.type) && !tab.templateOptions.genericSettingTab" [label]="tab?.templateOptions?.label">
        <formly-field [field]="tab"></formly-field>
    </mat-tab>
    </ng-container>
  </mat-tab-group>
  `,
})
export class AdminTabTypeComponent extends FieldType {
  tabTypes = ['array', 'object', 'rawobject'];
}
