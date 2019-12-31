import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { FieldType } from '@ngx-formly/material/form-field';
import { cloneDeep } from 'lodash';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'json-object-type',
  template: `
    <formly-field *ngFor="let f of field.fieldGroup" [field]="f"></formly-field>
    <input [value]="_value">
    <mat-form-field #formfield>
        <mat-label>{{ to.label }}</mat-label>
        <textarea matInput #textbox
        [class.hide-text]="true"
        [id]="id"
        [name]="to.title"
        [readonly]="to.readonly"
        [formControl]="formControl"
        [errorStateMatcher]="errorStateMatcher"
        [value]="_value"
        [placeholder]="to.placeholder"
        [tabindex]="to.tabindex || 0"
        [readonly]="to.readonly"
        >
        </textarea>
        <mat-hint *ngIf="to?.description && (!to?.errorMessage)"
        align="end" [innerHTML]="to?.description"></mat-hint>
    </mat-form-field>
        `,
        // <div class="highlighted-overlay" [class.show-overlay]="true" [innerHtml]="modelValue | highlightVariables"></div>
  styles: [`
    mat-card:last-child {
        display: none;
    }
  `],
})
export class JsonObjectTypeComponent extends FieldType implements OnDestroy, OnInit, AfterViewInit {
    defaultOptions = {
        defaultValue: {},
    };
    _value = '';
    thing;

    ngOnInit() {
        this.thing = cloneDeep(this.field)
        this.thing.model = JSON.stringify(this.field.parent.model, null, 2);
        this._value = JSON.stringify(this.field.parent.model, null, 2);
    }

    ngAfterViewInit() {
        this.formControl.valueChanges.subscribe()
    }
}
