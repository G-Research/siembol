import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { ChangeDetectorRef, Component, NgZone, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { FieldType } from '@ngx-formly/material/form-field';
import { Subject } from 'rxjs';
import { debounceTime, take, takeUntil } from 'rxjs/operators';
import { cloneDeep } from 'lodash';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-json-object-type',
  template: `
    <mat-form-field [style.width]="'100%'" #formfield>
      <mat-label *ngIf="to.label">
        {{ to.label }}
        <span *ngIf="to.required" class="mat-form-field-required-marker">*</span>
      </mat-label>
      <span class="row">
        <textarea
          matInput
          cdkTextareaAutosize
          #autosize="cdkTextareaAutosize"
          spellcheck="false"
          [errorStateMatcher]="errorStateMatcher"
          [formControl]="formControl"
          appRawjsonDirective
        >
        </textarea>
        <ng-container *ngIf="valid; else invalidJson">
          <re-json-tree [json]="tree"></re-json-tree>
        </ng-container>
        <ng-template #invalidJson> <mat-icon>cancel</mat-icon> json is invalid </ng-template>
      </span>
      <ng-container matSuffix *ngIf="to.suffix">
        <ng-container *ngTemplateOutlet="to.suffix"></ng-container>
      </ng-container>
      <mat-hint *ngIf="to?.description && !to?.errorMessage" align="end" [innerHTML]="to?.description"> </mat-hint>
    </mat-form-field>
  `,
  styles: [
    `
      mat-card:last-child {
        display: none;
      }

      textarea {
        width: 70%;
        padding: 5px;
        font-family: monospace;
        box-sizing: content-box;
      }

      re-json-tree {
        padding-left: 20px;
        max-width: 500px;
      }

      .row {
        display: flex;
      }
    `,
  ],
})
export class JsonObjectTypeComponent extends FieldType implements OnInit, OnDestroy {
  @ViewChild('autosize', { static: false }) autosize: CdkTextareaAutosize;
  defaultOptions = {
    defaultValue: {},
  };
  valid = true;
  tree = {};

  private ngUnsubscribe: Subject<any> = new Subject();

  constructor(private changeDetector: ChangeDetectorRef, private ngZone: NgZone) {
    super();
  }

  ngOnInit() {
    const key = Array.isArray(this.field.key) ? this.field.key[0] : this.field.key;
    this.tree = cloneDeep(this.field.parent.model[key]);
    this.changeDetector.markForCheck();
    this.formControl.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(s => {
      if (s) {
        this.formControl.setErrors(null);
        this.valid = true;
        this.tree = s;
        this.changeDetector.markForCheck();
      } else {
        this.valid = false;
        this.formControl.setErrors({ invalid: true });
        this.changeDetector.markForCheck();
      }
    });
  }

  triggerResize() {
    this.ngZone.onStable.pipe(take(1)).subscribe(() => this.autosize.resizeToFitContent(true));
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
