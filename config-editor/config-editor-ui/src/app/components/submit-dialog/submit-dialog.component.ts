import { Component, Inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StatusCode } from '@app/commons/status-code';
import { EditorResult, ExceptionInfo } from '@app/model';
import { Observable } from 'rxjs';
import { EditorService } from '../../services/editor.service';

@Component({
    selector: 're-submit-dialog',
    styleUrls: ['submit-dialog.component.scss'],
    templateUrl: 'submit-dialog.component.html',
})
export class SubmitDialogComponent implements OnInit {
    configValidity$: Observable<EditorResult<ExceptionInfo>>;
    message: string;
    exception: string;
    statusCode: string;
    validating = true;
    isValid = false;
    configName: string;

    constructor(public dialogref: MatDialogRef<SubmitDialogComponent>,
        private editorService: EditorService,
        @Inject(MAT_DIALOG_DATA) public data: string) {
        this.configName = data;
        this.configValidity$ = this.editorService.configStore.validateEditedConfig();
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
        this.editorService.configStore.submitEditedConfig();
        this.dialogref.close();
    }

    onClickClose() {
        this.dialogref.close();
    }
}
