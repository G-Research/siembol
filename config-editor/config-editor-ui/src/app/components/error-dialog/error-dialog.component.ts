import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
    selector: 're-error-dialog',
    styleUrls: ['error-dialog.component.scss'],
    templateUrl: 'error-dialog.component.html',
})
export class ErrorDialogComponent {
    constructor(public dialogref: MatDialogRef<ErrorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
        console.log(data);
    }

    onClickClose() {
        this.dialogref.close();
    }
}
