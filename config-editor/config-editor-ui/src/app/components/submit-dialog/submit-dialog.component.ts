import { Component, Inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { SubmitDialogData } from '@app/model/config-model';


@Component({
    selector: 're-submit-dialog',
    styleUrls: ['submit-dialog.component.scss'],
    templateUrl: 'submit-dialog.component.html',
})
export class SubmitDialogComponent implements OnInit {
    private readonly HTTP_TIMEOUT = 20000;
    configValidity$: Observable<any>;
    name: string;
    type: string;
    statusCode: string;
    validating = true;
    isValid = false;
    submitting = false;
    failedSubmit = false;
    validate: () => Observable<any>;
    submit: () => Observable<any>;

    constructor(public dialogref: MatDialogRef<SubmitDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: SubmitDialogData) {
        this.name = data.name;
        this.type = data.type;
        this.validate = data.validate;
        this.submit = data.submit;
    }

    ngOnInit() {
        this.validate()
            .pipe(
                timeout(this.HTTP_TIMEOUT))
            .subscribe(
                v => {
                    if (v) {
                        this.isValid = true;
                        this.validating = false;
                    }
                },
                e => {
                    this.dialogref.close();
                    throw e;
                }
            );
    }

    onClickSubmit() {
        this.submitting = true;
        this.submit()
            .pipe(
                timeout(this.HTTP_TIMEOUT))
            .subscribe(
                success => {
                    if (success) {
                        this.dialogref.close(true);
                    }
                },
                e => {
                    this.dialogref.close();
                    throw e;
                }
            );
    }

    onClickClose() {
        this.dialogref.close();
    }

}
