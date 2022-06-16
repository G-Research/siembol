import { Component, ViewChild } from '@angular/core';
import { MatInput } from '@angular/material/input';
import { FieldTypeConfig } from '@ngx-formly/core';
import { FieldType } from '@ngx-formly/material/form-field';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-field-mat-input',
  template: `
    <input
      matInput
      spellcheck="false"
      [class.hide-text]="false"
      [name]="to.title"
      [id]="id"
      [readonly]="to.readonly"
      [type]="type || 'text'"
      [errorStateMatcher]="errorStateMatcher"
      [formControl]="formControl"
      [formlyAttributes]="field"
      [tabindex]="to.tabindex || 0"
      [placeholder]="to.placeholder"
    />
  `,
  styles: [
    `
      .hide-text {
        -webkit-text-fill-color: transparent;
      }

      .highlighted-overlay {
        position: absolute;
        top: 6px;
      }
    `,
  ],
})
export class InputTypeComponent extends FieldType<FieldTypeConfig> {
  @ViewChild(MatInput, { static: true }) formFieldControl!: MatInput;

  get type() {
    return this.to.type || 'text';
  }
}
