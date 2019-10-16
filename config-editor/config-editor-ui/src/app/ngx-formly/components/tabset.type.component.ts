import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-tabset-type',
  template:
  `<mat-tab-group animationDuration="0ms">
    <mat-tab *ngFor="let tab of field.fieldGroup" [label]="tab?.templateOptions?.label">
        <formly-field [field]="tab"></formly-field>
    </mat-tab>
  </mat-tab-group>
  `,
})
export class TabsetTypeComponent extends FieldType {
    defaultOptions = {
        defaultValue: {},
    };
}
