
import { Component } from '@angular/core';
import { FieldWrapper } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-wrapper-panel',
  template: `
    <mat-card>
      <mat-card-header>
      </mat-card-header>
      <mat-card-content>
        <ng-container #fieldComponent></ng-container>
      </mat-card-content>
    <mat-card>
  `,
  styles: [`
    .description {
        padding: 0 10px 10px 15px;
        font-size: 0.9em;
        color: rgba(255, 255, 255, 0.7);
    }

    .mat-card:nth-child(3) {
        display: none;
    }
  `],
})
export class PanelWrapperComponent extends FieldWrapper {
}
