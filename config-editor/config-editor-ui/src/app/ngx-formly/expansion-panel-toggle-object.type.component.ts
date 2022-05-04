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
        <mat-panel-description class="description">
          <div class="description-container">
            {{ getFirstDescriptionField() }}
          </div>
        </mat-panel-description>
        <formly-field [field]="enabledField" (click)="onClickToggle($event)"></formly-field>
      </mat-expansion-panel-header>
      <div *ngFor="let f of field.fieldGroup">
        <div class="alert alert-danger" role="alert" *ngIf="showError && formControl.errors">
            <formly-validation-message [field]="field"></formly-validation-message>
        </div>
        <formly-field *ngIf="f.key !== isEnabledFieldName" [field]="f"></formly-field>
      </div>
    </mat-expansion-panel>
  `,
  styles: [
    `
      mat-card:last-child {
        display: none;
      }
      .description { 
        min-width: 0;
        min-height: 0;
      }
      .description-container {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
      }
    `,
  ],
})
export class ExpansionPanelToggleObjectTypeComponent extends FieldType implements OnInit {
    defaultOptions = {
      defaultValue: {},
    };
    isEnabledFieldName = "is_enabled";
    enabledField: FormlyFieldConfig;
     
    ngOnInit() {
        this.enabledField = this.field.fieldGroup.find(f => f.key == this.isEnabledFieldName); 
        this.enabledField.wrappers = [];  
        this.enabledField.type = "toggle";
    }

    onClickToggle(event: Event) {
      // note: this is to avoid the expansion panel opening/closing when toggling is_enabled
      event.stopPropagation();
    }

    getFirstDescriptionField(): string {
      if (this.to.descriptionFields) {
        for (const fieldName of this.to.descriptionFields) {
          if (this.model && this.model[fieldName]) {
            return this.model[fieldName]
          } 
        }
      }
    }
}
