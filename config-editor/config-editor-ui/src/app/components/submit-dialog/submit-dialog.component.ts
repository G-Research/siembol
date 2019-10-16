import { Component, Inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { StatusCode } from '@app/commons/status-code';
import { ConfigData, ConfigWrapper, EditorResult, ExceptionInfo } from '@app/model';
import { Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { Observable } from 'rxjs';

@Component({
    selector: 're-submit-dialog',
    styleUrls: ['submit-dialog.component.scss'],
    templateUrl: 'submit-dialog.component.html',
})
export class SubmitDialogComponent implements OnInit {
    config: ConfigWrapper<ConfigData>;
    configValidity$: Observable<EditorResult<ExceptionInfo>>;
    message: string;
    exception: string;
    statusCode: string;
    validating = true;
    isValid = false;

    constructor(public dialogref: MatDialogRef<SubmitDialogComponent>,
        private store: Store<fromStore.State>,
        @Inject(MAT_DIALOG_DATA) public data: ConfigWrapper<ConfigData>) {
        this.configValidity$ = this.store.select(fromStore.getConfigValidity);
        this.config = data;
        this.store.dispatch(new fromStore.ValidateConfig(this.config));
    }

    ngOnInit() {
        this.configValidity$.subscribe(v => {
            if (v !== undefined) {
                this.statusCode = v.status_code;
                if (v.status_code !== StatusCode.OK) {
                    this.message = v.attributes.message;
                    this.exception = v.attributes.exception;
                }
                this.validating = false;
                this.isValid = this.statusCode === StatusCode.OK;
            }
        });
    }

    onClickSubmit() {
        this.dialogref.close(this.config);
    }

    onClickClose() {
        this.dialogref.close();
    }
}
