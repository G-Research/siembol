import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, PullRequestInfo } from '@app/model';
import { Type, AdminConfig } from '@app/model/config-model';
import { PopupService } from '@app/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil, take, skip } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { BlockUI, NgBlockUI } from 'ng-block-ui';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-admin-editor',
    styleUrls: ['./admin.component.scss'],
    templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit, OnDestroy {
    public ngUnsubscribe = new Subject();
    public configData: ConfigData = {};
    public options: FormlyFormOptions = {};
    public form: FormGroup = new FormGroup({});
    public adminConfig$: Observable<AdminConfig>;
    public config: AdminConfig;
    public serviceName: string;
    public adminPullRequestPending$: Observable<PullRequestInfo>;
    private readonly PR_OPEN_MESSAGE = 'A pull request is already open';
    private readonly BLOCKING_TIMEOUT = 30000;

    @Input() fields: FormlyFieldConfig[];
    @BlockUI() blockUI: NgBlockUI;
    constructor(public dialog: MatDialog, public snackbar: PopupService,
        private editorService: EditorService, private router: Router) {
        this.adminConfig$ = editorService.configStore.adminConfig$;
        this.adminPullRequestPending$ = this.editorService.configStore.adminPullRequestPending$;
        this.serviceName = editorService.serviceName;
    }

    ngOnInit() {
        this.adminConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(config => {
            this.config = config;
            this.configData = this.editorService.adminSchema.wrapConfig(config.configData);
            this.editorService.adminSchema.wrapAdminConfig(this.configData);
            this.options.formState = {
                mainModel: this.configData,
                rawObjects: {},
            }
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    updateConfigInStore() {
        const configToClean = cloneDeep(this.config) as AdminConfig;
        configToClean.configData = cloneDeep(this.form.value);
        const configToUpdate = this.editorService.adminSchema.unwrapAdminConfig(configToClean);
        this.editorService.configStore.updateAdmin(configToUpdate);
    }

    onSubmit() {
        this.updateConfigInStore();
        this.adminPullRequestPending$.pipe(skip(1), take(1)).subscribe(a => {
            if (!a.pull_request_pending) {
                const dialogRef = this.dialog.open(SubmitDialogComponent,
                    {
                        data: {
                            type: Type.ADMIN_TYPE,
                            validate: () => this.editorService.configStore.validateAdminConfig(),
                            submit: () => this.editorService.configStore.submitAdminConfig()
                        },
                        disableClose: true
                    });

                dialogRef.afterClosed().subscribe(
                    success => {
                        if (success) {
                            this.router.navigate(
                                [this.editorService.serviceName, 'admin']
                            );
                        }
                    }
                );
            } else {
                this.snackbar.openNotification(this.PR_OPEN_MESSAGE);
            }
        });
    }

    public onSyncWithGit() {
        this.blockUI.start("loading admin config");
        this.editorService.configStore.reloadAdminConfig().subscribe(() => {
            this.blockUI.stop();
        });
        setTimeout(() => {
            this.blockUI.stop();
        }, this.BLOCKING_TIMEOUT);
    }
}
