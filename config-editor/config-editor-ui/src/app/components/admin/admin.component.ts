import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, PullRequestInfo } from '@app/model';
import { Type, AdminConfig } from '@app/model/config-model';
import { PopupService } from '@app/services/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil, take, skip, debounceTime } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigHistoryService } from '@app/services/config-history.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-admin-editor',
  styleUrls: ['./admin.component.scss'],
  templateUrl: './admin.component.html',
  providers: [ConfigHistoryService],
})
export class AdminComponent implements OnInit, OnDestroy {
  @Input() field: FormlyFieldConfig;
  @BlockUI() blockUI: NgBlockUI;
  ngUnsubscribe = new Subject();
  configData: ConfigData = {};
  options: FormlyFormOptions = {};
  form: FormGroup = new FormGroup({});
  adminConfig$: Observable<AdminConfig>;
  config: AdminConfig;
  serviceName: string;
  adminPullRequestPending$: Observable<PullRequestInfo>;
  markHistoryChange = false;
  private readonly PR_OPEN_MESSAGE = 'A pull request is already open';

  constructor(
    public dialog: MatDialog,
    public snackbar: PopupService,
    private editorService: EditorService,
    private router: Router,
    private configService: AppConfigService,
    private cd: ChangeDetectorRef,
    private configHistoryService: ConfigHistoryService
  ) {
    this.adminConfig$ = editorService.configStore.adminConfig$;
    this.adminPullRequestPending$ = this.editorService.configStore.adminPullRequestPending$;
    this.serviceName = editorService.serviceName;
  }

  ngOnInit() {
    this.adminConfig$.pipe(take(1)).subscribe(config => {
      this.config = config;
      //NOTE: in the form we are using wrapping config to handle optionals, unions
      if (config !== null) {
        this.updateAndWrapConfigData(this.editorService.adminSchema.wrapConfig(config.configData));
      }
    });
    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.ngUnsubscribe)).subscribe(values => {
      if (this.form.valid && !this.markHistoryChange) {
        this.addToConfigHistory(cloneDeep(values), this.field.templateOptions.tabIndex);
        this.updateConfigInStore(values);
      }
      this.markHistoryChange = false;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  updateConfigInStoreFromForm() {
    this.updateConfigInStore(this.form.value);
  }

  updateAndWrapConfigData(configData: ConfigData) {
    this.configData = cloneDeep(configData);
    this.editorService.adminSchema.wrapAdminConfig(this.configData);
    this.cd.markForCheck();
  }

  onSubmit() {
    this.updateConfigInStoreFromForm();
    this.adminPullRequestPending$.pipe(skip(1), take(1)).subscribe(a => {
      if (!a.pull_request_pending) {
        const dialogRef = this.dialog.open(SubmitDialogComponent, {
          data: {
            type: Type.ADMIN_TYPE,
            validate: () => this.editorService.configStore.validateAdminConfig(),
            submit: () => this.editorService.configStore.submitAdminConfig(),
          },
          disableClose: true,
        });

        dialogRef.afterClosed().subscribe(success => {
          if (success) {
            this.router.navigate([this.editorService.serviceName, 'admin']);
          }
        });
      } else {
        this.snackbar.openNotification(this.PR_OPEN_MESSAGE);
      }
    });
  }

  onSyncWithGit() {
    this.blockUI.start('loading admin config');
    this.editorService.configStore.reloadAdminConfig().subscribe(() => {
      this.adminConfig$.pipe(take(1)).subscribe(config => {
        this.config = config;
        if (config !== null) {
          this.updateAndWrapConfigData(this.editorService.adminSchema.wrapConfig(config.configData));
        }
        this.configHistoryService.clear();
      });
      this.blockUI.stop();
    });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  undoConfig() {
    this.markHistoryChange = true;
    let nextState = this.configHistoryService.undoConfig();
    this.field.templateOptions.tabIndex = nextState.tabIndex;
    this.updateConfigInStore(nextState.formState);
    this.updateAndWrapConfigData(nextState.formState);
    this.form.updateValueAndValidity();
  }

  redoConfig() {
    this.markHistoryChange = true;
    let nextState = this.configHistoryService.redoConfig();
    this.field.templateOptions.tabIndex = nextState.tabIndex;
    this.updateConfigInStore(nextState.formState);
    this.updateAndWrapConfigData(nextState.formState);
    this.form.updateValueAndValidity();
  }

  addToConfigHistory(config: any, tabIndex: number = 0) {
    this.configHistoryService.addConfig(config, tabIndex);
  }

  private updateConfigInStore(configData: ConfigData) {
    const configToClean = cloneDeep(this.config) as AdminConfig;
    configToClean.configData = cloneDeep(configData);
    this.config = this.editorService.adminSchema.unwrapAdminConfig(configToClean);
    this.editorService.configStore.updateAdmin(this.config);
  }
}
