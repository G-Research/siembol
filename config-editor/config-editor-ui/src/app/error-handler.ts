import { ErrorHandler, Injectable, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusCode } from './model';
import { ErrorDialogComponent } from './components/error-dialog/error-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ErrorDialog, InputError } from './model/config-model';

@Injectable({
    providedIn: 'root',
})
export class GlobalErrorHandler implements ErrorHandler {
    constructor(public dialog: MatDialog, private ngZone: NgZone) { }

    handleError(data: Error) {
        const dialogData = new ErrorDialogBuilder().build(data);
        this.ngZone.run(() => {
            this.dialog.open(ErrorDialogComponent, { data: dialogData });
        });
    }
}

export class ErrorDialogBuilder {
    private message: string;
    private resolution = "Ask administrators for help";
    private icon_name = "report_problem";
    private icon_color = "red";
    private title = "Error Details";

    build(data: Error): ErrorDialog {
        data = data["rejection"] ? data["rejection"] : data;
        if (data instanceof HttpErrorResponse) {
            this.fromBackendErrorMessage(data);
        } else {
            this.fromInternalErrorMessage(data);
        }
        return { 
            message: this.message, 
            title: this.title, 
            resolution: this.resolution, 
            icon_name: this.icon_name, 
            icon_color: this.icon_color
        }
    }

    fromBackendErrorMessage(data: HttpErrorResponse) {
        this.message = data.error.message ? data.error.message
                : data.error.exception ? data.error.exception
                : data.error.error ? data.error.error
                : data.message
        this.resolution = data.error.resolution? data.error.resolution: this.resolution;
        this.title = data.error.title? data.error.title: this.title;

        switch(data.status) {
            case StatusCode.BAD_REQUEST:
                this.icon_name = "feedback";
                this.icon_color = "orange";
                break;
            case StatusCode.UNKNOWN_ERROR:
                this.message = "Cannot reach backend";
                break;
            case StatusCode.UNAUTHORISED:
                this.message = "Unauthorised user";
                this.resolution = "Close current tab and try in a new one. If error persists contact administrator";
                break;
        }
    }

    fromInternalErrorMessage(data: Error) {
        if (data instanceof InputError) {
            this.icon_name = "feedback";
            this.icon_color = "orange";
            this.resolution = "Inspect error message and try to fix your request. If error persists contact administrator";
        }
        this.message = data.toString();
    }
}