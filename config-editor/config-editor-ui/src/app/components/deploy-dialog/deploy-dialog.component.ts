import { UiMetadata } from '../../model/ui-metadata-map';

import { Component, Inject, TemplateRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { FormGroup } from '@angular/forms';
import { AppConfigService } from '@app/services/app-config.service';
import { EditorService } from '@services/editor.service';
import { ConfigData, Deployment } from '@app/model';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { cloneDeep } from 'lodash';
import { take, catchError } from 'rxjs/operators';
import { throwError, of } from 'rxjs';
import { DiffResults } from 'ngx-text-diff/lib/ngx-text-diff.model';
import { AppService } from '@app/services/app.service';
import { DeploymentWrapper, TestingType } from '@app/model/config-model';

@Component({
  selector: 're-deploy-dialog',
  styleUrls: ['deploy-dialog.component.scss'],
  templateUrl: 'deploy-dialog.component.html',
})
export class DeployDialogComponent {
  newDeployment: Deployment;
  newContent: ConfigData;
  initContent: ConfigData;
  environment: string;
  isValid = undefined;
  message: string;
  validating = false;
  hasChanged = true;
  exception: string;
  statusCode: string;
  deploymentSchema = {};
  serviceName: string;
  uiMetadata: UiMetadata;
  extrasData = {};
  testingType = TestingType.DEPLOYMENT_TESTING;

  testEnabled = false;
  public options: FormlyFormOptions = { formState: {} };

  field: FormlyFieldConfig;
  public form: FormGroup = new FormGroup({});

  private readonly OUTDATED_DEPLOYMENT_MESSAGE = `Old version detected, latest deployment 
        have now been reloaded. Please prepare your deployment again.`;
  private readonly INVALID_MESSAGE = 'Deployment is invalid.';
  private readonly MAX_HEIGHT = '90vh';

  constructor(
    public dialogref: MatDialogRef<DeployDialogComponent>,
    private config: AppConfigService,
    public dialog: MatDialog,
    private service: EditorService,
    private formlyJsonSchema: FormlyJsonschema,
    private appService: AppService,
    @Inject(MAT_DIALOG_DATA) public data: Deployment
  ) {
    this.serviceName = service.serviceName;
    this.uiMetadata = this.appService.getUiMetadataMap(this.serviceName);
    this.newDeployment = data;
    if (this.uiMetadata.deployment.extras !== undefined) {
      this.field = this.formlyJsonSchema.toFieldConfig(service.configSchema.createDeploymentSchema());
      this.extrasData = this.uiMetadata.deployment.extras.reduce((a, x) => ({ ...a, [x]: this.newDeployment[x] }), {});
    } else {
      this.validating = true;
      this.service.configLoader
        .validateRelease(this.newDeployment)
        .pipe(take(1))
        .pipe(
          catchError(e => {
            this.dialogref.close();
            return throwError(e);
          })
        )
        .subscribe(s => {
          this.validating = false;
          if (s) {
            this.isValid = true;
          } else {
            this.service.configStore.reloadStoreAndDeployment();
            this.dialogref.close();
            throw this.OUTDATED_DEPLOYMENT_MESSAGE;
          }
        });
    }
    this.testEnabled = this.uiMetadata.testing.deploymentTestEnabled;
    this.environment = this.config.environment;

    this.service.configStore.initialDeployment$.subscribe((d: Deployment) => {
      this.initContent = this.getDeploymentMetadataString(d);
    });
    this.newContent = this.getDeploymentMetadataString(this.newDeployment);
  }

  getDeploymentMetadataString(deployment: Deployment): string {
    return (
      Object.values(deployment.configs)
        .map((c: ConfigData) => `${c.name} (v${c.version} ${c.author})`)
        .join('\n') + '\n'
    );
  }

  onValidate() {
    this.validating = true;
    this.newDeployment = { ...this.newDeployment, ...this.extrasData };
    this.service.configLoader
      .getRelease()
      .flatMap((d: DeploymentWrapper) => {
        if (d.storedDeployment.deploymentVersion > this.newDeployment.deploymentVersion) {
          return of(false);
        }
        return this.service.configLoader.validateRelease(this.newDeployment);
      })
      .pipe(take(1))
      .pipe(
        catchError(e => {
          this.isValid = false;
          this.message = this.INVALID_MESSAGE;
          return throwError(e);
        })
      )
      .subscribe(s => {
        this.validating = false;
        if (s) {
          this.isValid = true;
        } else {
          this.service.configStore.reloadStoreAndDeployment().subscribe(() => {
            this.dialogref.close();
            throw this.OUTDATED_DEPLOYMENT_MESSAGE;
          });
        }
      });
  }

  onClickDeploy() {
    const deployment =
      this.extrasData !== undefined
        ? Object.assign(cloneDeep(this.newDeployment), this.extrasData)
        : this.newDeployment;
    this.dialogref.close(deployment);
  }

  onClickTest(templateRef: TemplateRef<any>) {
    this.dialog.open(templateRef, {
      maxHeight: this.MAX_HEIGHT,
    });
  }

  onClickClose() {
    this.dialogref.close();
  }

  updateOutput(event) {
    this.extrasData = event;
  }

  onCompareResults(diffResults: DiffResults) {
    if (!diffResults.hasDiff) {
      this.hasChanged = false;
    }
  }
}
