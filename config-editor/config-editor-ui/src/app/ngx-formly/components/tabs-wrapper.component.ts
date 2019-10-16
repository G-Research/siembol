import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { FieldWrapper } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-wrapper-tabs',
  template:
  `<mat-tab-group>
    <mat-tab *ngFor="let tab of field.fieldGroup" [label]="to.label">
        <ng-template #fieldComponent></ng-template>
    </mat-tab>
  </mat-tab-group>
  `,
})
export class TabsWrapperComponent extends FieldWrapper {
    @ViewChild('fieldComponent', { read: ViewContainerRef, static: true }) fieldComponent: ViewContainerRef;
}
