import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { ChangeDetectorRef, Component, NgZone, OnInit, ViewChild } from '@angular/core';
import { FieldType } from '@ngx-formly/material/form-field';
import { Subject } from 'rxjs';
import { debounceTime, take } from 'rxjs/operators';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'json-object-type',
  template: `
    <span class="row">
        <textarea matInput cdkTextareaAutosize #autosize="cdkTextareaAutosize"
          spellcheck="false" [ngModel]="val" (ngModelChange)="jsonChange$.next($event)" [errorStateMatcher]="errorStateMatcher">
        </textarea>
        <ng-container *ngIf="valid; else invalidJson">
            <re-json-tree [json]="tree"></re-json-tree>
        </ng-container>
        <ng-template #invalidJson>
            <mat-icon>cancel</mat-icon> json is invalid
        </ng-template>
    </span>
    `,
  styles: [`
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
  `],
})
export class JsonObjectTypeComponent extends FieldType implements OnInit {
    defaultOptions = {
        defaultValue: {},
    };
    valid = true;
    val: string;
    _val: string;
    jsonChange$: Subject<string> = new Subject<string>();
    tree: object = {};
    @ViewChild('autosize', {static: false}) autosize: CdkTextareaAutosize;

    constructor(private changeDetector: ChangeDetectorRef, private ngZone: NgZone) {
        super();
    }

    ngOnInit() {
        this.val = JSON.stringify({[Array.isArray(this.field.key) ? this.field.key[0] : this.field.key]: this.field.parent.model[Array.isArray(this.field.key) ? this.field.key[0] : this.field.key]}, null, 2);
        this.tree = this.field.parent.model;
        this.formControl.setValidators = a => {
            try {
                JSON.parse(this._val);

                return null;
            } catch (e) {
              return {invalidJson: true}
            }
        };
        this.changeDetector.markForCheck();
        this.jsonChange$.pipe(debounceTime(500)).subscribe(s => {
            this._val = s;
            try {
                const parsed = JSON.parse(s);
                if (parsed) {
                    this.formControl.setErrors(null);
                    this.valid = true;
                    this.tree = parsed;
                    this.options.formState['rawObjects'][Array.isArray(this.field.key) ? this.field.key[0] : this.field.key] = parsed[Array.isArray(this.field.key) ? this.field.key[0]: this.field.key];
                    this.changeDetector.markForCheck();
                }
            } catch (ex) {
                this.valid = false;
                this.tree = {};
                this.formControl.setErrors({invalid: true});
                this.changeDetector.markForCheck();
            }
        })
    }

    triggerResize() {
        this.ngZone.onStable.pipe(take(1)).subscribe(() => this.autosize.resizeToFitContent(true));
    }
}
