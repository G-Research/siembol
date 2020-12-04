import { Component } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';
import { isNullOrUndefined, assignModelValue, getKeyPath, clone } from '../util/utility.functions';
import { cloneDeep } from 'lodash';


@Component({
  selector: 'formly-tab-array',
  template: `
    <div>
        <mat-tab-group animationDuration="0ms" [(selectedIndex)]="selectedIndex">
            <mat-tab *ngFor="let tab of field.fieldGroup; let i = index" [label]="getEvaluatorType(tab?.model)">
                <span class="align-right">
                    
                    <svg *ngIf="i === model.length - 1"
                        xmlns="http://www.w3.org/2000/svg"
                        height="18" width="18" viewBox="0 0 24 24"
                        class="close-button"
                        (click)="remove(i)">
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
                    </svg>
                </span>
                <formly-field [field]="tab"></formly-field>
            </mat-tab>
        </mat-tab-group>
        <div class="align-right">
            <button mat-raised-button color="primary" (click)="add(model.length)">add evaluator</button>
        </div>
    </div>
  `,
  styles: [`
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
  `],
})
export class TabArrayTypeComponent extends FieldArrayType {
    public selectedIndex = 0;

    getEvaluatorType(model) {
        return Object.keys(model)[0];
    }

    add(i?: number, initialModel?: any, { markAsDirty } = { markAsDirty: true }) {
        i = isNullOrUndefined(i) ? this.field.fieldGroup.length : i;
        if (!this.model) {
          assignModelValue(this.field.parent.model, getKeyPath(this.field), []);
        }
        let originalModel = cloneDeep(this.model);
        
        this.model.splice(i, 0, initialModel ? clone(initialModel) : undefined);
    
        (<any> this.options)._buildForm(true);
        originalModel.splice(i, 0, this.model[this.model.length - 1]);
        for (let i=0; i < this.model.length; i++) {
            this.model[i] = originalModel[i];
        }
        markAsDirty && this.formControl.markAsDirty();
        this.selectedIndex = i;
    }
}