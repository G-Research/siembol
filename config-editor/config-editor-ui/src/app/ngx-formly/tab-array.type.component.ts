import { Component } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';

@Component({
  selector: 'formly-tab-array',
  template: `
    <div>
      <mat-tab-group animationDuration="0ms" [(selectedIndex)]="selectedTab">
        <mat-tab *ngFor="let tab of field.fieldGroup; let i = index">
          <ng-template mat-tab-label>
            <mat-icon *ngIf="i != 0" (click)="moveDown(i)">arrow_left</mat-icon>
            {{ getUnionType(i) }}
            <mat-icon *ngIf="i != field.fieldGroup.length - 1" (click)="moveUp(i)">arrow_right</mat-icon>
          </ng-template>
          <span class="align-right">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              height="18"
              width="18"
              viewBox="0 0 24 24"
              class="close-button"
              (click)="remove(i)"
            >
              <path
                d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"
              />
            </svg>
          </span>
          <formly-field [field]="tab"></formly-field>
        </mat-tab>
      </mat-tab-group>
      <div class="align-right">
        <button mat-raised-button color="primary" (click)="add(selectedTab + 1)">add</button>
      </div>
    </div>
  `,
  styles: [
    `
      .close-button {
        cursor: pointer;
        top: 6px;
        right: 6px;
        fill: orange;
        z-index: 500;
      }

      .move-arrow {
        width: 14px;
        height: 14px;
        font-size: 18px;
        cursor: pointer;
      }

      .greyed-out {
        width: 14px;
        height: 14px;
        font-size: 18px;
        color: #707070;
        cursor: default;
      }

      .align-right {
        margin-left: auto;
        margin-right: 0;
        display: table;
        padding-top: 5px;
      }

      ::ng-deep .mat-tab-label {
        padding: 0 4px;
      }

      ::ng-deep .mat-tab-label .mat-tab-label-content {
        width: 100%;
        justify-content: space-around;
      }
    `,
  ],
})
export class TabArrayTypeComponent extends FieldArrayType {
  selectedTab = 0;

  getUnionType(index: number): string {
    return this.form.value.evaluators[index].evaluator_type;
  }


  add(index: number, unionModel = undefined) {
    super.add(index, unionModel);
    this.selectedTab = index;
    this.options.build();
  }

  moveDown(index: number) {
    if (index === 0) {
      return;
    }

    this.reorder(index - 1, index);
  }

  moveUp(index: number) {
    if (index === this.model.length - 1) {
      return;
    }

    this.reorder(index, index + 1);
  }

  private reorder(oldIndex: number, newIndex: number) {
    const unionModel = this.model[oldIndex];
    this.remove(oldIndex);
    this.add(newIndex, unionModel);
  }

}
