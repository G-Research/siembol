import { UiMetadata } from '../../model/ui-metadata-map';

import { AfterViewChecked, ChangeDetectorRef, Component, Inject, TemplateRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { FormGroup } from '@angular/forms';
import { AppConfigService } from '@app/services/app-config.service';
import { EditorService } from '@services/editor.service';
import { ConfigData, Release } from '@app/model';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { cloneDeep } from 'lodash';
import { take, catchError } from 'rxjs/operators';
import { throwError, of, mergeMap } from 'rxjs';
import { DiffResults } from 'ngx-text-diff/lib/ngx-text-diff.model';
import { AppService } from '@app/services/app.service';
import { ReleaseWrapper, TestingType } from '@app/model/config-model';


@Component({
  selector: 're-release-dialog',
  styleUrls: ['release-dialog.component.scss'],
  templateUrl: 'release-dialog.component.html',
})
export class ReleaseDialogComponent implements AfterViewChecked {
  newRelease: Release;
  newContent: ConfigData;
  initContent: ConfigData;
  environment: string;
  isReleaseValid = undefined;
  message: string;
  validating = false;
  hasChanged = true;
  exception: string;
  statusCode: string;
  releaseSchema = {};
  serviceName: string;
  uiMetadata: UiMetadata;
  extrasData = {};
  testingType = TestingType.RELEASE_TESTING;
  testEnabled = false;
  options: FormlyFormOptions = { formState: {} };

  field: FormlyFieldConfig;
  form: FormGroup = new FormGroup({});

  private isDiffReduced = false;
  private readonly OUTDATED_RELEASE_MESSAGE = `Old version detected, latest release 
        have now been reloaded. Please prepare your release again.`;
  private readonly INVALID_MESSAGE = 'Release is invalid.';
  private readonly MAX_HEIGHT = '90vh';

  constructor(
    public dialogref: MatDialogRef<ReleaseDialogComponent>,
    private config: AppConfigService,
    public dialog: MatDialog,
    private service: EditorService,
    private formlyJsonSchema: FormlyJsonschema,
    private appService: AppService,
    private cd: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: Release
  ) {
    this.serviceName = service.serviceName;
    this.uiMetadata = this.appService.getUiMetadataMap(this.serviceName);
    this.newRelease = data;
    if (this.uiMetadata.release.extras !== undefined) {
      this.field = this.formlyJsonSchema.toFieldConfig(service.configSchema.createReleaseSchema());
      this.extrasData = this.uiMetadata.release.extras.reduce((a, x) => ({ ...a, [x]: this.newRelease[x] }), {});
      this.form.valueChanges.subscribe(() => {
        this.isReleaseValid = undefined;
      })
    } else {
      this.validating = true;
      this.service.configLoader
        .validateRelease(this.newRelease)
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
            this.isReleaseValid = true;
          } else {
            this.service.configStore.reloadStoreAndRelease();
            this.dialogref.close();
            throw this.OUTDATED_RELEASE_MESSAGE;
          }
        });
    }
    this.testEnabled = this.uiMetadata.testing.releaseTestEnabled;
    this.environment = this.config.environment;

    this.service.configStore.initialRelease$.subscribe((d: Release) => {
      this.initContent = this.getReleaseMetadataString(d);
    });
    this.newContent = this.getReleaseMetadataString(this.newRelease);
  }

  getReleaseMetadataString(release: Release): string {
    return (
      Object.values(release.configs)
        .map((c: ConfigData) => `${c.name} (v${c.version} ${c.author})`)
        .join('\n') + '\n'
    );
  }

  onValidate() {
    this.validating = true;
    this.newRelease = { ...this.newRelease, ...this.extrasData };
    this.service.configLoader
      .getRelease()
      .pipe(
        mergeMap((d: ReleaseWrapper) => {
          if (d.storedRelease.releaseVersion > this.newRelease.releaseVersion) {
            return of(false);
          }
          return this.service.configLoader.validateRelease(this.newRelease);
        }),
        take(1),
        catchError(e => {
          this.isReleaseValid = false;
          this.message = this.INVALID_MESSAGE;
          return throwError(e);
        })
      )
      .subscribe(s => {
        this.validating = false;
        if (s) {
          this.isReleaseValid = true;
        } else {
          this.service.configStore.reloadStoreAndRelease().subscribe(() => {
            this.dialogref.close();
            throw this.OUTDATED_RELEASE_MESSAGE;
          });
        }
      });
  }

  onClickRelease() {
    const release =
      this.extrasData !== undefined
        ? Object.assign(cloneDeep(this.newRelease), this.extrasData)
        : this.newRelease;
    this.dialogref.close(release);
  }

  onClickTest(templateRef: TemplateRef<any>) {
    this.dialog.open(templateRef, {
      maxHeight: this.MAX_HEIGHT,
    });
  }

  onClickClose() {
    this.dialogref.close();
  }

  onCompareResults(diffResults: DiffResults) {
    if (!diffResults.hasDiff) {
      this.hasChanged = false;
    }
  }

  ngAfterViewChecked() {
    const showDiffs = document.getElementById("showDiffs");
    if (showDiffs && !this.isDiffReduced && !showDiffs["checked"]) {
      this.isDiffReduced = true;
      showDiffs.click();
      this.cd.detectChanges();
    }
  }
}
