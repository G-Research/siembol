import { UiMetadataMap } from '../../model/ui-metadata-map';

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';
import { StatusCode } from '@app/commons';

import { FormGroup } from '@angular/forms';
import { AppConfigService } from '@app/config';
import { EditorService } from '@app/editor.service';
import { ConfigData, ConfigWrapper, Deployment } from '@app/model';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import * as fromStore from 'app/store';
import { cloneDeep } from 'lodash';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { TestingDialogComponent } from '../testing/testing-dialog/testing-dialog.component';

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
    serviceName$: Observable<string>;
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
        private store: Store<fromStore.State>,
        private service: EditorService,
        private formlyJsonSchema: FormlyJsonschema,
        @Inject(MAT_DIALOG_DATA) public data: Deployment<ConfigWrapper<ConfigData>>) {
        this.store.select(fromStore.getServiceName).pipe(take(1)).subscribe(r => {
            this.validating = false;
            this.serviceName = r;
            this.uiMetadata = this.config.getUiMetadata(r);
            if (this.uiMetadata.deployment.extras !== undefined) {
                this.fields = [this.formlyJsonSchema.toFieldConfig(this.createDeploymentSchema(r))];
            } else {
                this.service.getLoader(this.serviceName).validateRelease(data).pipe(take(1))
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
        });

        this.testEnabled = this.uiMetadata.testing.deploymentTestEnabled;
        this.deployment = data;
        this.environment = this.config.environment;
    }

    private createDeploymentSchema(serviceName: string): string {
        const depSchema = this.service.getLoader(serviceName).originalSchema;
        depSchema.properties[this.uiMetadata.deployment.config_array] = {};
        delete depSchema.properties[this.uiMetadata.deployment.config_array];
        delete depSchema.properties[this.uiMetadata.deployment.version];
        depSchema.required = depSchema.required.filter(element => {
            if (element !== this.uiMetadata.deployment.version && element !== this.uiMetadata.deployment.config_array) {
                return true;
            }

            return false;
        });

        return depSchema;
    }

    onValidate() {
        this.deployment = {...this.deployment, ...this.extrasData};
        this.service.getLoader(this.serviceName)
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
        this.dialog.open(TestingDialogComponent, {
            data: {
                configDto: this.deployment,
                singleConfig: false,
            },
        })
    }

    onClickClose() {
        this.dialogref.close();
    }

    updateOutput(event) {
        this.extrasData = event;
    }
}
