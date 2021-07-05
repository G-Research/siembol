import { Component, OnInit } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';

@Component({
  selector: 'formly-tab-array',
  template: `
    <div>
      <mat-tab-group animationDuration="0ms" [(selectedIndex)]="selectedTab" (selectedTabChange)="onTabChange()">
        <mat-tab *ngFor="let tab of field.fieldGroup; let i = index" [label]="getUnionType(tab?.model)">
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
    `,
  ],
})
export class TabArrayTypeComponent extends FieldArrayType implements OnInit {
  selectedTab = 0;

  getUnionType(model): string {
    const keys = Object.keys(model);
    return keys[keys.length - 1];
  }

  add(i: number) {
    const modelLength = this.model ? this.model.length : 0;
    super.add(modelLength);
    this.selectedTab = i;
    for (let j = this.model.length - 1; j >= i; j--) {
      this.moveDown(j);
    }
  }

  moveDown(i: number) {
    if (i === this.model.length - 1) {
      return;
    }

    this.reorder(i, i + 1);
  }

  ngOnInit() {
    this.selectedTab = this.field.templateOptions.tabIndex;
  }

  onTabChange() {
    this.field.templateOptions.tabIndex = this.selectedTab;
  }

  private reorder(oldI: number, newI: number) {
    this.reorderFields(this.field.fieldGroup, oldI, newI);
    this.reorderItems(this.model, oldI, newI);
    this.reorderItems(this.formControl.controls, oldI, newI);
  }

  private reorderItems(obj: any[], oldI: number, newI: number) {
    const old = obj[oldI];
    obj[oldI] = obj[newI];
    obj[newI] = old;
  }

  private reorderFields(obj: any[], oldI: number, newI: number) {
    const old = obj[oldI];
    obj[oldI] = obj[newI];
    obj[oldI].key = `${oldI}`;

    obj[newI] = old;
    obj[newI].key = `${newI}`;
  }
}
