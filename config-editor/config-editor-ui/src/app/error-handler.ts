import { ErrorHandler, Injectable, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusCode } from './model';
import { ErrorDialogComponent } from './components/error-dialog/error-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { InputError } from './model/config-model';

@Injectable({
    providedIn: 'root',
})
export class GlobalErrorHandler implements ErrorHandler {
    constructor(public dialog: MatDialog, private ngZone: NgZone) { }

    handleError(data: Error | HttpErrorResponse) {
        let message: string;
        let resolution = "Ask administrators for help";
        let icon_name = "report_problem";
        let icon_color = "red";
        let title = "Error Details";
        data = data["rejection"] ? data["rejection"] : data;
        if (data instanceof HttpErrorResponse) {
            message = data.error.message ? data.error.message
                : data.error.exception ? data.error.exception
                : data.error.error ? data.error.error
                : data.message
            resolution = data.error.resolution? data.error.resolution: resolution;
            title = data.error.title? data.error.title: title;

            if (data.status === StatusCode.BAD_REQUEST) {
                icon_name = "feedback";
                icon_color = "orange";
            } else if (data.status === StatusCode.UNKNOWN_ERROR) {
                message = "Cannot reach backend";
            } else if (data.status === StatusCode.UNAUTHORISED) {
                message = "Unauthorised user";
                resolution = "Close current tab and try in a new one. If error persists contact administrator";
            }
        } else {
            if (data instanceof InputError) {
                icon_name = "feedback";
                icon_color = "orange";
                resolution = "Inspect error message and try to fix your request. If error persists contact administrator"
            }
            message = data.toString();
        }
        
        this.ngZone.run(() => {
            this.dialog.open(ErrorDialogComponent,
                {
                    data: { message, title, resolution, icon_name, icon_color }
                });
        });
    }
}