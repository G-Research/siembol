import { Component, Inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StatusCode } from '@app/commons/status-code';
import { EditorResult, ExceptionInfo } from '@app/model';
import { Observable, of, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { SubmitDialogData } from '@app/model/config-model';


@Component({
    selector: 're-submit-dialog',
    styleUrls: ['submit-dialog.component.scss'],
    templateUrl: 'submit-dialog.component.html',
})
export class SubmitDialogComponent implements OnInit {
    private readonly HTTP_TIMEOUT = 10000;
    configValidity$: Observable<EditorResult<ExceptionInfo>>;
    name: string;
    type: string;
    message: string;
    statusCode: string;
    validating = true;
    isValid = false;
    submitting = false;
    failedSubmit = false;
    validate: () => Observable<EditorResult<ExceptionInfo>>;
    submit: () => Observable<void>;

    constructor(public dialogref: MatDialogRef<SubmitDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: SubmitDialogData) {
        this.name = data.name;
        this.type = data.type;
        this.validate = data.validate;
        this.submit = data.submit;
        this.configValidity$ = this.validate();
    }

    ngOnInit() {
        this.configValidity$
            .pipe(
                timeout(this.HTTP_TIMEOUT),
                catchError(this.handleError)
            )
            .subscribe(v => {
            if (v !== undefined) {
                this.statusCode = v.status_code;
                if (v.status_code !== StatusCode.OK) {
                    this.message = v.attributes.message;
                } else {
                    this.validating = false;
                    this.isValid = true;
                }
            }
        });
    }

    onClickSubmit() {
        this.submitting = true;
        this.submit()
            .pipe(
                timeout(this.HTTP_TIMEOUT),
                catchError((e: ExceptionInfo) => {
                    this.failedSubmit = true;

                    return this.handleError(e);
                }))
            .subscribe(
                success => {
                    if (success) {
                        this.dialogref.close(true);
                    }
                }
            );
    }

    onClickClose() {
        this.dialogref.close();
    }

    handleError(e: ExceptionInfo) {
        this.message = e.message;
        const status = e.status;
        if (status === StatusCode.BAD_REQUEST) {

            return of(undefined)
        } else if (status === StatusCode.ERROR) {
            this.message = 'Server error, please contact administrator.\n' + this.message;
        }

        return throwError(e);
    }
}
