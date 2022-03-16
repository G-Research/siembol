import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CheckboxEvent } from "@app/model/config-model";
import { CheckboxConfig } from "@app/model/ui-metadata-map";

@Component({
  selector: "re-checkbox-filters",
  template: `
  <div class="container">
    <mat-list *ngFor="let checkboxFilter of checkboxFilters | keyvalue">
      <span mat-subheader>{{ checkboxFilter.key | titlecase }}</span>
        <mat-checkbox 
        *ngFor="let singleCheckbox of checkboxFilter.value | keyvalue" 
        (change)="clickCheckbox($event.checked, checkboxFilter.key, singleCheckbox.key)"
        >
        {{ singleCheckbox.key | titlecase }}
        </mat-checkbox>
    </mat-list>
  </div>
  `,
  styles: [`
    .container {
      height: calc(100vh - 65px);
      background: #2a2a2a;
      display: flex;
      flex-direction: column;
      align-items: stretch;
      padding: 10px 30px;
      outline: 2;
    }
    mat-checkbox {
      display: block;
    }
    mat-list {
      margin: 5px 10px
    }
    `,
  ],
})
export class CheckboxFiltersComponent {
  @Input() checkboxFilters: CheckboxConfig;
  @Output() readonly selectedCheckbox: EventEmitter<CheckboxEvent> = new EventEmitter<CheckboxEvent>();

  clickCheckbox(event: boolean, title: string, name: string) {
    this.selectedCheckbox.emit({ checked: event, title, name });
  }
}