import { Inject } from '@angular/core';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { AppConfigService } from '@app/config';
import { EditorService } from '@app/editor.service';
import { ConfigData, Deployment, EditorResult } from '@app/model';
import { PopupService } from '@app/popup.service';
import { Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { of, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { ConfigLoaderService } from '../../config-loader.service';
import { ConfigTestResult } from '../../model/config-model';

@Component({
  selector: 're-testing-dialog',
  styleUrls: ['testing-dialog.component.scss'],
  templateUrl: 'testing-dialog.component.html',
})
export class TestingDialogComponent implements OnDestroy, OnInit {
  deploymentConfig: Deployment<ConfigData>;
  alert: string;
  output: string;
  EVENT_HELP: string;
  testSuccess = 'none';
  private ngUnsubscribe = new Subject();
  private service: ConfigLoaderService;
  private env: string;
  isSingleConfig: boolean;

  constructor(public dialogref: MatDialogRef<TestingDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private editorService: EditorService,
    public snackbar: PopupService,
    private store: Store<fromStore.State>,
    private appConfig: AppConfigService) {
    this.store.select(fromStore.getServiceName).pipe(take(1)).subscribe(r => {
      this.env = r;
      this.service = this.editorService.getLoader(r);
      this.EVENT_HELP = this.appConfig.getUiMetadata(this.env).testing.helpMessage;
    })
    this.deploymentConfig = data.configDto;
    this.isSingleConfig = data.singleConfig;
  }

  ngOnInit() {
    this.store.select(fromStore.getConfigTestingEvent).pipe(take(1)).subscribe(e => this.alert = e);
  }

  public beginTest() {
    this.testSuccess = 'none';
    if (this.alert === null || this.alert === undefined || this.alert === '') {
      this.output = 'Error: please provide an alert to test the rule against';
      this.testSuccess = 'fail';

      return;
    }

    let event: any;
    try {
      // TODO remove this once test specification is done properly
      if (this.env === 'parserconfig') {
        event = {log: this.alert};
      } else {
        event = JSON.parse(this.alert);
      }
    } catch (e) {
      this.output = 'Error in alert JSON:\n\n' + e;
      this.testSuccess = 'fail';

      return;
    }

    const testDto: any = {
      files: [{
        content:
          this.deploymentConfig,
      }],
      event: event,
    };

    if (this.service) {
      if (this.isSingleConfig) {
        this.service.testSingleConfig(testDto).pipe(takeUntil(this.ngUnsubscribe))
          .map(r => r)
          .catch(err => {
            this.ngUnsubscribe.next();
            this.snackbar.openSnackBar(err, `Error could not contact ${this.env} backend`);

            return of(null);
          }).subscribe((b: EditorResult<ConfigTestResult>) => {
            if (b === null) {
              return;
            }
            if (b.status_code === 'OK') {
              this.output = b.attributes.test_result_output;
            } else {
              if (b.attributes.message && b.attributes.exception) {
                this.output = b.attributes.message + '\n' + b.attributes.exception;
              } else if (b.attributes.message !== undefined) {
                this.output = b.attributes.message;
              } else if (b.attributes.exception !== undefined) {
                this.output = b.attributes.exception;
              }
            }
            this.testSuccess = b.attributes.test_result_complete ? 'success' : 'fail';
          }
          )
      } else {
        this.service.testDeploymentConfig(testDto).pipe(takeUntil(this.ngUnsubscribe))
          .map(r => r)
          .catch(err => {
            this.ngUnsubscribe.next();
            this.snackbar.openSnackBar(err, `Error could not contact ${this.env} backend`);

            return of(null);
          }).subscribe((b: EditorResult<ConfigTestResult>) => {
            if (b === null) {
              return;
            }
            if (b.status_code === 'OK') {
              this.output = b.attributes.test_result_output;
            } else {
              if (b.attributes.message && b.attributes.exception) {
                this.output = b.attributes.message + '\n' + b.attributes.exception;
              } else if (b.attributes.message !== undefined) {
                this.output = b.attributes.message;
              } else if (b.attributes.exception !== undefined) {
                this.output = b.attributes.exception;
              }
            }
            this.testSuccess = b.attributes.test_result_complete ? 'success' : 'fail';
          }
          )
      }
    } else {
      this.snackbar.openSnackBar(`Error, could not load ${this.env} service correctly,
                this is likely due to not being able to fetch the service name`, 'Error loading testing service');
    }
  }

  public onTab($event) {
    return false;
  }

  onClickClose() {
    this.dialogref.close();
  }

  ngOnDestroy() {
    this.store.dispatch(new fromStore.StoreConfigTestingEvent(this.alert));
    this.ngUnsubscribe.next();
  }
}
