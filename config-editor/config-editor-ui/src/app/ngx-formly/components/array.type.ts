
import { Component } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-array-type',
  template: `
    <legend *ngIf="to.label">{{ to.label }}</legend>
    <p class="description" *ngIf="to.description">{{ to.description }}</p>
    <div class="alert alert-danger" role="alert" *ngIf="showError && formControl.errors">
      <formly-validation-message [field]="field"></formly-validation-message>
    </div>
    <div *ngFor="let f of field.fieldGroup; let i = index;" class="row">
      <formly-field class="array-item" [field]="f"></formly-field>
        <div class="column">
            <a (click)="moveUp(i)">
                <mat-icon class="move-arrow" [class.greyed-out]="i <= 0">arrow_drop_up</mat-icon>
            </a>
            <a (click)="moveDown(i)">
                <mat-icon class="move-arrow" [class.greyed-out]="i >= field.fieldGroup.length -1">arrow_drop_down</mat-icon>
            </a>
        </div>
        <svg *ngIf="showRemoveButton"
            xmlns="http://www.w3.org/2000/svg"
            height="18" width="18" viewBox="0 0 24 24"
            class="close-button"
            (click)="remove(i)">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
        </svg>
    </div>
    <div class="row">
      <div class="add-button" *ngIf="to.maxItems ? field.fieldGroup.length < to.maxItems : true">
        <button mat-raised-button color="primary" (click)="add()">Add to {{to.label}}</button>
      </div>
    <div>
  `,
  styles: [`
    legend {
        font-weight: 400;
        margin: 5px;
        font-size: 1.3em;
    }

    .description {
        padding: 0 10px 10px 15px;
        font-size: 0.9em;
        color: rgba(255, 255, 255, 0.7);
    }

    .row {
        display: flex;
        padding-top: 5px;
    }

    .rhs {
        margin: 5px auto 5px 0;
    }

    .close-button {
        cursor: pointer;
        top: 6px;
        right: 6px;
        fill: orange;
        z-index: 500;
    }

    .add-button {
        margin: 5px 0 5px 0;
    }

    .column {
        display: block;
        width: 16px;
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

    .blank-icon {
        width: 14px;
        height: 14px;
        opacity: 0;
        cursor: default;
    }

    .close-button:hover { background: rgba(255,255,255,0.2); }
    .array-item:hover > .close-button { visibility: visible }
    .array-item {
        flex: 9;
        position: relative;
        transition: all 0.2s ease-in-out;
    }
  `],
})
export class ArrayTypeComponent extends FieldArrayType {
    public showRemoveButton = true;

    moveUp (index: number) {
        if (index > 0) {
            this.reorder(index, index - 1);
        }
    }

    moveDown (index: number) {
        if (index < this.field.fieldGroup.length - 1) {
            this.reorder(index, index + 1);
        }
    }

    reorder (oldIndex: number, newIndex: number) {
        const temp = this.model[oldIndex];
        this.remove(oldIndex);
        this.add(newIndex, temp);
    }
}
