import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-object-type',
  template: `
    <div class="alert alert-danger" role="alert" *ngIf="showError && formControl.errors">
      <formly-validation-message [field]="field"></formly-validation-message>
    </div>
    <formly-field *ngFor="let f of field.fieldGroup" [field]="f"></formly-field>
  `,
  styles: [`
    mat-card:last-child {
        display: none;
    }
  `],
})
export class ObjectTypeComponent extends FieldType {
    defaultOptions = {
        defaultValue: {},
    };
}
