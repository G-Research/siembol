import { UiMetadataMap } from '../../model/ui-metadata-map';

import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StatusCode } from '@app/commons';

import { FormGroup } from '@angular/forms';
import { AppConfigService } from '@app/config';
import { EditorService } from '@services/editor.service';
import { ConfigData, ConfigWrapper, Deployment } from '@app/model';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { cloneDeep } from 'lodash';
import { take } from 'rxjs/operators';

@Component({
    selector: 're-deploy-dialog',
    styleUrls: ['deploy-dialog.component.scss'],
    templateUrl: 'deploy-dialog.component.html',
})
export class DeployDialogComponent {
    deployment: Deployment<ConfigWrapper<ConfigData>>;
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
        @Inject(MAT_DIALOG_DATA) public data: Deployment<ConfigWrapper<ConfigData>>) {
        this.serviceName = service.serviceName;
        this.validating = false;
        this.uiMetadata = this.config.getUiMetadata(this.serviceName);
        if (this.uiMetadata.deployment.extras !== undefined) {
            this.fields = [this.formlyJsonSchema.toFieldConfig(service.configLoader.createDeploymentSchema())];
        } else {
            this.service.configLoader.validateRelease(data).pipe(take(1))
                .subscribe(s => {
                    if (s !== undefined) {
                        this.statusCode = s.status_code;
                        if (s.status_code !== StatusCode.OK) {
                            this.message = s.attributes.message;
                            this.exception = s.attributes.exception;
                        }
                        this.validating = false;
                        this.isValid = s.status_code === StatusCode.OK ? true : false;
                    }
                });
        }


        this.testEnabled = this.uiMetadata.testing.deploymentTestEnabled;
        this.deployment = data;
        this.environment = this.config.environment;
    }

    onValidate() {
        this.deployment = {...this.deployment, ...this.extrasData};
        this.service.configLoader
            .validateRelease(this.deployment).pipe(take(1)).subscribe(s => {
                if (s !== undefined) {
                    this.statusCode = s.status_code;
                    if (s.status_code !== StatusCode.OK) {
                        this.message = s.attributes.message;
                        this.exception = s.attributes.exception;
                    }
                    this.validating = false;
                    this.isValid = s.status_code === StatusCode.OK ? true : false;
                }
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
