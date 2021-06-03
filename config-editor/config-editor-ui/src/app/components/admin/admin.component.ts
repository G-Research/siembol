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
import { takeUntil, take, skip } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigHistory } from '@app/model/store-state';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-admin-editor',
  styleUrls: ['./admin.component.scss'],
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit, OnDestroy {
  @Input() fields: FormlyFieldConfig[];
  @BlockUI() blockUI: NgBlockUI;
  ngUnsubscribe = new Subject();
  configData: ConfigData = {};
  options: FormlyFormOptions = {};
  form: FormGroup = new FormGroup({});
  adminConfig$: Observable<AdminConfig>;
  config: AdminConfig;
  private history: ConfigHistory = { past: [], future: [] };
  serviceName: string;
  adminPullRequestPending$: Observable<PullRequestInfo>;
  private inUndoRedo = false;
  private readonly PR_OPEN_MESSAGE = 'A pull request is already open';
  constructor(
    public dialog: MatDialog,
    public snackbar: PopupService,
    private editorService: EditorService,
    private router: Router,
    private configService: AppConfigService,
    private cd: ChangeDetectorRef
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
        this.updateAndWrapConfigData(config.configData);
        this.options.formState = {
          mainModel: cloneDeep(this.configData),
          rawObjects: {},
        };
      }
    });
  }

  ngAfterViewInit() {
    this.form.valueChanges.subscribe(values => {
      if (
        this.form.valid &&
        !this.inUndoRedo &&
        (this.history.past.length == 0 || JSON.stringify(this.history.past[0].formState) !== JSON.stringify(values))
      ) {
        this.history.past.splice(0, 0, {
          formState: cloneDeep(values),
          tabIndex: this.fields[0].templateOptions.tabIndex,
        });
        this.history.future = [];
        this.updateConfigInStore(values);
      }
      this.inUndoRedo = false;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  updateConfigInStoreFromForm() {
    this.updateConfigInStore(this.form.value);
  }

  private updateConfigInStore(configData: ConfigData) {
    const configToClean = cloneDeep(this.config) as AdminConfig;
    configToClean.configData = cloneDeep(configData);
    // configToClean.configData = this.editorService.adminSchema.cleanRawObjects(
    //   configToClean.configData,
    //   this.options.formState.rawObjects
    // );
    this.config = this.editorService.adminSchema.unwrapAdminConfig(configToClean);
    this.editorService.configStore.updateAdmin(this.config);
  }

  updateAndWrapConfigData(configData: ConfigData) {
    this.configData = this.editorService.adminSchema.wrapConfig(configData);
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
      this.blockUI.stop();
    });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  undoConfigInStore() {
    this.inUndoRedo = true;
    this.history.future.splice(0, 0, {
      formState: cloneDeep(this.configData),
      tabIndex: this.fields[0].templateOptions.tabIndex,
    });
    this.history.past.shift();
    this.fields[0].templateOptions.tabIndex = this.history.past[0].tabIndex;
    this.updateConfigInStore(this.history.past[0].formState);
    this.updateAndWrapConfigData(this.config.configData);
  }

  redoConfig() {
    this.inUndoRedo = true;
    this.fields[0].templateOptions.tabIndex = this.history.future[0].tabIndex;
    this.updateConfigInStore(this.history.future[0].formState);
    this.updateAndWrapConfigData(this.config.configData);
    this.history.past.splice(0, 0, {
      formState: cloneDeep(this.history.future[0].formState),
      tabIndex: this.history.future[0].tabIndex,
    });
    this.history.future.shift();
  }
}
