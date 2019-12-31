import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { StatusCode } from '@app/commons/status-code';
import { EditorService } from '@app/editor.service';
import { EditorResult, ExceptionInfo } from '@app/model';
import { TestCase } from '@app/model/test-case';
import { ValidationState } from '@app/model/validation-status';
import { throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Component({
    selector: 're-submit-testcase-dialog',
    styleUrls: ['submit-testcase-dialog.component.scss'],
    templateUrl: 'submit-testcase-dialog.component.html',
})
export class SubmitTestcaseDialogComponent implements OnInit {
    testcase: TestCase;
    message: string;
    exception: string;
    statusCode: string;
    validating = true;
    validation = ValidationState;
    validationState: ValidationState;
    errorResponse: HttpErrorResponse;

    constructor(public dialogref: MatDialogRef<SubmitTestcaseDialogComponent>,
        private service: EditorService,
        @Inject(MAT_DIALOG_DATA) public data: TestCase) {
        this.testcase = data;
    }

    ngOnInit() {
        this.service.validateTestCase(this.testcase).pipe(
            tap((r: EditorResult<ExceptionInfo>) => {
                if (r !== undefined) {
                    this.statusCode = r.status_code;
                    if (this.statusCode === StatusCode.OK) {
                        this.validationState = ValidationState.PASS;
                    } else {
                        this.message = r.attributes.message;
                        this.exception = r.attributes.exception;
                        this.validationState = ValidationState.FAIL;
                    }
                }
            }),
            catchError((err: HttpErrorResponse) => {
                this.validationState = ValidationState.ERROR;
                this.errorResponse = err;

                return throwError(err);
            })
        ).subscribe(a => a, err => console.error(err), () => this.validating = false);
    }

    onClickSubmit() {
        this.dialogref.close(this.testcase);
    }

    onClickClose() {
        this.dialogref.close();
    }
}
