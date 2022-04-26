import { Component } from "@angular/core";
import { ConfigStatus } from "@app/model/config-model";
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { ICellRendererParams } from '@ag-grid-community/core';
import { animate, state, style, transition, trigger } from '@angular/animations';

@Component({
  selector: "re-label-cell-renderer",
  styles: [`
    .labels_cell {
      display: flex;
      width: 100%;
    }
    .label-chip {
      display: inline-block;
      padding: 2px 4px;
      min-width: 40px;
      max-width: 90%;
      line-height: 20px;
      text-align: center;
      border-radius: 9999px;
      color: #111;
      background: #868686;
      font-family: monospace;
      margin-left: 10px;
      max-height: 20px;
      overflow: ellipsis;
      overflow: hidden;
    }
    .label-text {
      font-size: 8pt;
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
      max-width: 400px;
    }
    .label-expand {
      align-items:top;
      display: flex;
      justify-content:flex-end;
    }
    `,
  ],
  template: `
  <div [@grow]="labelsState" (@grow.done)="growDone()" class="labels_cell" #labels_cell>
    <div style="width:90%;">
      <div class="label-chip" [matTooltip]="label" *ngFor="let label of labels">
        <div class="label-text" *ngIf="label !== null || label !== ''">
          {{label}}
        </div>    
      </div>
    </div> 
    <div class="label-expand">
      <a *ngIf="!expanded && checkOverflow(labels_cell)" (click)="toggleLabelsState()" [title]="'expand labels'">
        <mat-icon>expand_more</mat-icon>
      </a>
      <a *ngIf="expanded" (click)="toggleLabelsState()" [title]="'collapse labels'">
        <mat-icon>expand_less</mat-icon>
      </a>
    </div>
  </div>
  `,
  animations: [
    trigger('grow', [
      state('out', style({
        maxHeight: '1500px',
      })),
      state('in', style({
        overflow: 'hidden',
        maxHeight: '40px',
      })),
    ]),
  ],
})
export class LabelCellRendererComponent implements ICellRendererAngularComp {
  status: ConfigStatus;
  configStatusEnum = ConfigStatus;
  labels: string[];
  expanded = true;
  labelsState = 'in';
  private params: any;

  agInit(params: any): void {
    this.params = params;
    this.labels = params.node.data.labels;
  }

  refresh(params: ICellRendererParams) {
    this.params = params;
    return true
  }

  checkOverflow (element) {
    return element.offsetHeight < element.scrollHeight ||
           element.offsetWidth < element.scrollWidth;
  }

  toggleLabelsState() {
    this.labelsState = this.labelsState === 'out' ? 'in' : 'out';
  }

  growDone() {
    this.expanded = !this.expanded;
  }
}
