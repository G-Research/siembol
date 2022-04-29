import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ErrorDialog } from '@app/model/config-model';

@Component({
    selector: 're-error-dialog',
    styleUrls: ['error-dialog.component.scss'],
    templateUrl: 'error-dialog.component.html',
})
export class ErrorDialogComponent {
    constructor(public dialogref: MatDialogRef<ErrorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ErrorDialog) {
    }

    onClickClose() {
        this.dialogref.close();
    }
}
