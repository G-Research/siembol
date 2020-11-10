import { UiMetadataMap } from '../../model/ui-metadata-map';

import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StatusCode } from '@app/commons';

import { FormGroup } from '@angular/forms';
import { AppConfigService } from '@app/config';
import { EditorService } from '@services/editor.service';
import { ConfigData, Config, Deployment } from '@app/model';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { cloneDeep } from 'lodash';
import { take, catchError } from 'rxjs/operators';
import { throwError, of } from 'rxjs';
import { AppService } from '@app/services/app.service';

@Component({
    selector: 're-deploy-dialog',
    styleUrls: ['deploy-dialog.component.scss'],
    templateUrl: 'deploy-dialog.component.html',
})
export class DeployDialogComponent {
    deployment: Deployment;
    environment: string;
    isValid = undefined;
    validating = true;
    message: string;
    exception: string;
    statusCode: string;
    deploymentSchema = {};
    serviceName: string;
    uiMetadata: UiMetadataMap;
    extrasData = {};

    testEnabled = false;
    public options: FormlyFormOptions = {formState: {}};

    fields: FormlyFieldConfig[];
    public form: FormGroup = new FormGroup({});

    constructor(public dialogref: MatDialogRef<DeployDialogComponent>,
        private config: AppConfigService,
        public dialog: MatDialog,
        private service: EditorService,
        private formlyJsonSchema: FormlyJsonschema,
        private appService: AppService,
        @Inject(MAT_DIALOG_DATA) public data: Deployment) {
        this.serviceName = service.serviceName;
        this.validating = false;
        this.uiMetadata = this.appService.getUiMetadataMap(this.serviceName);
        if (this.uiMetadata.deployment.extras !== undefined) {
            this.fields = [this.formlyJsonSchema.toFieldConfig(service.configSchema.createDeploymentSchema())];
        } else {
            this.service.configLoader.validateRelease(data).pipe(take(1))
                .pipe(
                    catchError(e => {
                        this.isValid = false;
                        return throwError(e);
                }))
                .subscribe(() => {
                    this.isValid = true;
                    this.validating = false;
                });
        }


        this.testEnabled = this.uiMetadata.testing.deploymentTestEnabled;
        this.deployment = data;
        this.environment = this.config.environment;
    }

    onValidate() {
        this.deployment = {...this.deployment, ...this.extrasData};
        this.service.configLoader
            .validateRelease(this.deployment).pipe(take(1))
            .pipe(
                catchError(e => {
                    this.isValid = false;
                    return throwError(e);
            }))
            .subscribe(() => {
                this.isValid = true;
                this.validating = false;
        });
    }

    onClickDeploy() {
        const deployment = this.extrasData !== undefined
            ? Object.assign(cloneDeep(this.deployment), this.extrasData)
            : this.deployment;
        this.dialogref.close(deployment);
    }

    onClickTest() {
        //TODO: add testing deployment
    }

    onClickClose() {
        this.dialogref.close();
    }

    updateOutput(event) {
        this.extrasData = event;
    }
}
