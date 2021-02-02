import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { AfterViewInit, Component, ElementRef, NgZone, OnDestroy, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatInput } from '@angular/material/input';
import { FieldType } from '@ngx-formly/material/form-field';
import { Subject } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-field-mat-textarea',
  template: `
    <div class="overlay-holder">
        <mat-form-field #formfield>
            <mat-label>{{ to.label }}</mat-label>
            <textarea class="text-area" highlight matInput #textbox cdkTextareaAutosize #autosize="cdkTextareaAutosize"
            spellcheck="false"
            [class.hide-text]="true"
            [id]="id"
            [name]="to.title"
            [readonly]="to.readonly"
            [formControl]="formControl"
            [errorStateMatcher]="errorStateMatcher"
            [formlyAttributes]="field"
            [placeholder]="to.placeholder"
            [tabindex]="to.tabindex || 0"
            [readonly]="to.readonly"
            >
            </textarea>
            <mat-hint *ngIf="to?.description && (!to?.errorMessage)"
            align="end" [innerHTML]="to?.description"></mat-hint>
        </mat-form-field>
        <div class="highlighted-overlay" [class.show-overlay]="true" [innerHtml]="value | highlightVariables"></div>
    </div>
  `,
  styles: [`
    .text-area {
        resize: none;
        line-height: normal;
        overflow: hidden;
    }

    mat-form-field {
        width: 100%;
    }

    .hide-text {
        -webkit-text-fill-color: transparent;
    }

    .show-overlay {
        display: block;
        width: 100%;
        overflow-wrap: break-word;
        white-space: pre-wrap;
    }

    .highlighted-overlay {
        position: absolute;
        top: 21px;
        left: 0;
        z-index: 10;
    }

    .overlay-holder {
        position: relative;
        line-height: normal;
        overflow: hidden;
    }

    ::ng-deep .overlay-holder .mat-input-element {
        position: relative;
        z-index: 20;
    }
  `],
})
export class TextAreaTypeComponent extends FieldType implements OnDestroy, AfterViewInit {
  @ViewChild(MatInput, {static: false}) formFieldControl!: MatInput;

  @ViewChild('textbox', {static: true}) textbox: ElementRef;
  @ViewChild('formfield', {static: true}) formfield: FormControl;
  @ViewChild('autosize', {static: false}) autosize: CdkTextareaAutosize;

  public displayOverlay = true;

  private ngUnsubscribe: Subject<any> = new Subject();

  constructor(private ngZone: NgZone) {
      super();
  }

  triggerResize() {
    this.ngZone.onStable.pipe(take(1)).subscribe(() => this.autosize.resizeToFitContent(true));
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
