import { Component } from '@angular/core';
import { FieldWrapper } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-expansion-panel-wrapper',
  template: `
    <mat-expansion-panel [expanded]="true">
      <mat-expansion-panel-header>
        <mat-panel-title>
          {{ to.label }}
        </mat-panel-title>
        <mat-panel-description>
          {{ to.description }}
        </mat-panel-description>
      </mat-expansion-panel-header>
      <ng-template matExpansionPanelContent #fieldComponent></ng-template>
    </mat-expansion-panel>
  `,
})
export class ExpansionPanelWrapperComponent extends FieldWrapper {}
