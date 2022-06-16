import { Component, ViewChild, NgZone, AfterViewInit } from '@angular/core';
import { FieldType } from '@ngx-formly/material/form-field';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { take } from 'rxjs/operators';
import { FieldTypeConfig } from '@ngx-formly/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-field-mat-textarea',
  template: `
    <textarea
      cdkTextareaAutosize
      #autosize="cdkTextareaAutosize"
      class="text-area"
      highlight
      matInput
      spellcheck="false"
      [id]="id"
      [name]="to.title"
      [readonly]="to.readonly"
      [formControl]="formControl"
      [errorStateMatcher]="errorStateMatcher"
      [formlyAttributes]="field"
      [placeholder]="to.placeholder"
      [tabindex]="to.tabindex || 0"
    >
    </textarea>
  `,
  styles: [
    `
      .text-area {
        resize: none;
        line-height: normal;
        overflow: hidden;
        margin: 0;
        width: 100%;
      }

      .hide-text {
        -webkit-text-fill-color: transparent;
      }

      .highlighted-overlay {
        position: absolute;
        line-height: normal;
        top: 8.5px;
        left: 0;
        z-index: 10;
        width: 100%;
        overflow-wrap: break-word;
        white-space: pre-wrap;
      }

      ::ng-deep .mat-input-element {
        position: relative;
        z-index: 20;
      }
    `,
  ],
})
export class TextAreaTypeComponent extends FieldType<FieldTypeConfig>  implements AfterViewInit {
  @ViewChild('autosize') autosize: CdkTextareaAutosize;

  constructor(private ngZone: NgZone) {
    super();
  }

  ngAfterViewInit() {
    this.triggerResize();
  }

  triggerResize() {
    this.ngZone.onStable.pipe(take(1)).subscribe(() => this.autosize.resizeToFitContent(true));
  }
}
