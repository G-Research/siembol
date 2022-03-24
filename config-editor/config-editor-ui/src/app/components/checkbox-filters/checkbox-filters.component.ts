import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CheckboxEvent, ServiceFilters } from "@app/model/config-model";
import { FilterConfig } from "@app/model/ui-metadata-map";

@Component({
  selector: "re-checkbox-filters",
  template: `
  <div class="container">
    <mat-list *ngFor="let checkboxFilter of checkboxFilters | keyvalue">
      <span mat-subheader>{{ checkboxFilter.key.replace('_', ' ') | titlecase }}</span>
        <mat-checkbox 
        *ngFor="let singleCheckbox of checkboxFilter.value | keyvalue" 
        [(ngModel)]="selectedCheckboxes[checkboxFilter.key + '|' + singleCheckbox.key]"
        (change)="clickCheckbox($event.checked, checkboxFilter.key, singleCheckbox.key)"
        >
        {{ singleCheckbox.key.replace('_', ' ') | titlecase }}
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
    .mat-checkbox {
      display: block;
      padding-left: 5px;
    }
    .mat-list {
      margin: 5px 10px;
    }
    .mat-subheader {
      font-size: 20px;
      padding: 0;
    }
    /* .mat-checkbox-layout {
      color: #b5b5b5;
    } */
    `,
  ],
})
export class CheckboxFiltersComponent {
  @Input() checkboxFilters: FilterConfig;
  @Input() selectedCheckboxes: ServiceFilters;
  @Output() readonly selectedCheckbox: EventEmitter<CheckboxEvent> = new EventEmitter<CheckboxEvent>();

  clickCheckbox(event: boolean, group_name: string, name: string) {
    this.selectedCheckbox.emit({ checked: event, name: group_name + "|" + name });
  }
}