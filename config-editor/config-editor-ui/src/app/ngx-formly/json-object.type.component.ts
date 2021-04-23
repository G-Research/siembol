import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { ChangeDetectorRef, Component, NgZone, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { FieldType } from '@ngx-formly/material/form-field';
import { Subject } from 'rxjs';
import { debounceTime, take, takeUntil } from 'rxjs/operators';
import { MatInput } from '@angular/material/input';
import { FormControl } from '@angular/forms';

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
          [ngModel]="val"
          (ngModelChange)="jsonChange$.next($event)"
          [errorStateMatcher]="errorStateMatcher"
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
  @ViewChild(MatInput, { static: false }) formFieldControl!: MatInput;
  @ViewChild('formfield', { static: true }) formfield: FormControl;
  defaultOptions = {
    defaultValue: {},
  };
  valid = true;
  val: string;
  _val: string;
  jsonChange$: Subject<string> = new Subject<string>();
  tree = {};

  private ngUnsubscribe: Subject<any> = new Subject();

  constructor(private changeDetector: ChangeDetectorRef, private ngZone: NgZone) {
    super();
  }

  ngOnInit() {
    const key = Array.isArray(this.field.key) ? this.field.key[0] : this.field.key;
    const conf = this.field.parent.model[key];
    this.val = JSON.stringify(conf, null, 2);
    this.tree = conf;
    this.formControl.setValidators = () => {
      try {
        JSON.parse(this._val);

        return null;
      } catch (e) {
        return { invalidJson: true };
      }
    };
    this.changeDetector.markForCheck();
    this.jsonChange$.pipe(debounceTime(500), takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this._val = s;
      try {
        const parsed = JSON.parse(s);
        if (parsed) {
          this.formControl.setErrors(null);
          this.valid = true;
          this.tree = parsed;
          const path = this.getFieldPath(key);
          this.options.formState['rawObjects'][path] = parsed;
          this.changeDetector.markForCheck();
        }
      } catch (ex) {
        this.valid = false;
        this.tree = {};
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

  private getFieldPath(key: string | number): string {
    let field = this.field;
    const keys = [];
    keys.push(key);
    while (field.parent.key) {
      keys.push(field.parent.key);
      field = field.parent;
    }
    return keys.reverse().join('.');
  }
}
