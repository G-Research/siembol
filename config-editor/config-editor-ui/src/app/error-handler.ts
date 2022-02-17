import { ErrorHandler, Injectable, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusCode } from './model';
import { ErrorDialogComponent } from './components/error-dialog/error-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Injectable({
    providedIn: 'root',
})
export class GlobalErrorHandler implements ErrorHandler {
    constructor(public dialog: MatDialog, private ngZone: NgZone) { }

    handleError(data: Error | HttpErrorResponse) {
        let message: string;
        data = data["rejection"] ? data["rejection"] : data;
        if (data instanceof HttpErrorResponse) {
            message = data.error.message ? data.error.message
                : data.error.exception ? data.error.exception
                : data.message
            if (data.status === StatusCode.ERROR) {
                message = 'Server error, please contact administrator.\n' + message;
            } else if (data.status === StatusCode.UNKNOWN_ERROR) {
                message = "Cannot reach backend, please contact administrator"
            }
        } else {
            message = data.toString();
        }
        
        this.ngZone.run(() => {
            this.dialog.open(ErrorDialogComponent,
                {
                    data: message,
                });
        });
    }
}