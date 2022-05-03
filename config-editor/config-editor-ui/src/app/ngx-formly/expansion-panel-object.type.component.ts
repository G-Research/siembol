import { Component, OnInit } from '@angular/core';
import { FieldType, FormlyFieldConfig } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-expansion-panel-toggle-object-type',
  template: `
    <mat-expansion-panel [expanded]="true" hideToggle>
      <mat-expansion-panel-header>
        <mat-panel-title>
          {{ to.label }}
        </mat-panel-title>
        <mat-panel-description>
          {{ model?.description }}
        </mat-panel-description>
        <formly-field [field]="enabled_field" (click)="onClickToggle($event)"></formly-field>
      </mat-expansion-panel-header>
      <div *ngFor="let f of field.fieldGroup">
        <div class="alert alert-danger" role="alert" *ngIf="showError && formControl.errors">
            <formly-validation-message [field]="field"></formly-validation-message>
        </div>
        <formly-field *ngIf="f.key !== 'is_enabled'" [field]="f"></formly-field>
      </div>
    </mat-expansion-panel>
  `,
  styles: [
    `
      mat-card:last-child {
        display: none;
      }
    `,
  ],
})
export class ExpansionPanelToggleObjectTypeComponent extends FieldType implements OnInit {
    defaultOptions = {
      defaultValue: {},
    };
    isEnabledFieldName = "is_enabled";
    enabled_field: FormlyFieldConfig;
     
    ngOnInit() {
        this.enabled_field = this.field.fieldGroup.find(f => f.key == this.isEnabledFieldName); 
        this.enabled_field.wrappers = [];  
        this.enabled_field.type = "toggle";
    }

    onClickToggle(event: Event) {
      // note: this is to avoid the expansion panel opening/closing when toggling is_enabled
      event.stopPropagation();
    }
}
