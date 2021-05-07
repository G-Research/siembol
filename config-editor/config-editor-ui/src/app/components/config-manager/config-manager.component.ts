import { animate, query, stagger, style, transition, trigger } from '@angular/animations';
import {
    CdkDrag,
    CdkDragDrop,
    CdkDropList,
} from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { Config, Deployment, PullRequestInfo } from '@app/model';
import { PopupService } from '@app/services/popup.service';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { skip, take, takeUntil } from 'rxjs/operators';
import { DeployDialogComponent } from '../deploy-dialog/deploy-dialog.component';
import { JsonViewerComponent } from '../json-viewer/json-viewer.component';
import { FileHistory } from '../../model';
import { ConfigStoreService } from '../../services/store/config-store.service';
import { Router } from '@angular/router';
import { BlockUI, NgBlockUI } from 'ng-block-ui';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-config-manager',
    styleUrls: ['./config-manager.component.scss'],
    templateUrl: './config-manager.component.html',
    animations: [
        trigger('list', [
            transition(':enter', [
                transition('* => *', []),
                query(':enter', [
                    style({ opacity: 0 }),
                    stagger(10, [
                        style({ transform: 'scale(0.8)', opacity: 0 }),
                        animate('.6s cubic-bezier(.8,-0.6,0.7,1.5)',
                            style({ transform: 'scale(1)', opacity: 1 })),
                    ]),
                ], { optional: true }),
            ]),
        ]),
    ],
})
export class ConfigManagerComponent implements OnInit, OnDestroy {
    private ngUnsubscribe = new Subject();
    private configStore: ConfigStoreService;
    public allConfigs$: Observable<Config[]>;
    public filteredConfigs$: Observable<Config[]>;
    public deployment$: Observable<Deployment>;
    public deployment: Deployment;
    public configs: Config[];
    public selectedConfig$: Observable<number>;
    public selectedConfig: number;
    public pullRequestPending$: Observable<PullRequestInfo>;
    public releaseSubmitInFlight$: Observable<boolean>;
    public searchTerm$: Observable<string>;
    public filteredDeployment: Deployment;
    public filteredDeployment$: Observable<Deployment>;
    private filteredConfigs: Config[];
    public filterMyConfigs$: Observable<boolean>;
    public filterUndeployed$: Observable<boolean>;
    public filterUpgradable$: Observable<boolean>;
    public deploymentHistory$: Observable<FileHistory[]>;
    public deploymentHistory;

    private readonly BLOCKING_TIMEOUT = 30000;
    private readonly PR_OPEN_MESSAGE = 'A pull request is already open';
    @BlockUI() blockUI: NgBlockUI;
    constructor(public dialog: MatDialog, private snackbar: PopupService,
        private editorService: EditorService, private router: Router) {
        this.configStore = editorService.configStore;
        this.allConfigs$ = this.configStore.allConfigs$;

        this.filteredConfigs$ = this.configStore.filteredConfigs$;

        this.pullRequestPending$ = this.configStore.pullRequestPending$;
        this.releaseSubmitInFlight$ = this.configStore.releaseSubmitInFlight$;
        this.deployment$ = this.configStore.deployment$;

        this.searchTerm$ = this.configStore.searchTerm$;
        this.filteredDeployment$ = this.configStore.filteredDeployment$;
        this.filterMyConfigs$ = this.configStore.filterMyConfigs$;

        this.filterUndeployed$ = this.configStore.filterUndeployed$;
        this.filterUpgradable$ = this.configStore.filterUpgradable$;

        this.deploymentHistory$ = this.configStore.deploymentHistory$;
    }

    ngOnInit() {
        this.deployment$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
            this.deployment = cloneDeep(s);
        });
        this.allConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(r => {
            this.configs = cloneDeep(r);
        });

        this.filteredConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => this.filteredConfigs = s);

        this.filteredDeployment$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
            this.filteredDeployment = cloneDeep(s);
        });

        this.deploymentHistory$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(h => this.deploymentHistory = { fileHistory: h });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    public onSearch(searchTerm: string) {
        this.configStore.updateSearchTerm(searchTerm);
    }

    public upgrade(index: number) {
        this.configStore.upgradeConfigInDeployment(index);
    }

    public drop(event: CdkDragDrop<Config[]>) {
        if (event.container.id === 'deployment-list') {
            if (event.previousContainer.id === 'store-list') {
                this.configStore.addConfigToDeploymentInPosition(event.previousIndex, event.currentIndex);
            } else if (event.previousContainer.id === 'deployment-list' && event.currentIndex !== event.previousIndex) {
                this.configStore.moveConfigInDeployment(event.previousIndex, event.currentIndex);
            }
        }
    }

    public onView(id: number, releaseId: number = undefined) {
        this.dialog.open(JsonViewerComponent, {
            data: {
                config1: releaseId === undefined
                    ? undefined
                    : this.filteredDeployment.configs[releaseId].configData,
                config2: this.filteredConfigs[id].configData,
            },
        });
    }

    public onEdit(id: number) {
        this.router.navigate(
            [this.editorService.serviceName, 'edit'],
            { queryParams: { configName: this.filteredConfigs[id].name } }
        );
    }

    public addToDeployment(id: number) {
        this.configStore.addConfigToDeployment(id);
    }

    public onClone(id: number) {
        this.router.navigate(
            [this.editorService.serviceName, 'edit'],
            { queryParams: { newConfig: true, cloneConfig: this.filteredConfigs[id].name } }
        );
    }

    public onRemove(id: number) {
        this.configStore.removeConfigFromDeployment(id);
    }

    public onClickCreate() {
        this.router.navigate(
            [this.editorService.serviceName, 'edit'],
            { queryParams: { newConfig: true } });
    }

    public onDeploy() {
        this.configStore.loadPullRequestStatus();
        this.pullRequestPending$.pipe(skip(1), take(1)).subscribe(a => {
            if (!a.pull_request_pending) {
                const dialogRef = this.dialog.open(DeployDialogComponent, {
                    data: cloneDeep(this.deployment),
                });
                dialogRef.afterClosed().subscribe((results: Deployment) => {
                    if (results && results.configs.length > 0) {
                        if (results.deploymentVersion >= 0) {
                            this.configStore.submitRelease(results);
                        }
                    }
                });
            } else {
                this.snackbar.openNotification(this.PR_OPEN_MESSAGE);
            }
        });
    }

    public onFilterMine($event: boolean) {
        this.configStore.updateFilterMyConfigs($event);
    }

    public onSyncWithGit() {
        this.blockUI.start("loading store and deployments");
        this.configStore.reloadStoreAndDeployment().subscribe(() => {
            this.blockUI.stop();
        });
        setTimeout(() => {
            this.blockUI.stop();
        }, this.BLOCKING_TIMEOUT);
    }

    public duplicateItemCheck(item: CdkDrag<Config>, deployment: CdkDropList<Config[]>) {
        return deployment.data.find(d => d.name === item.data.name) === undefined
            ? true : false;
    }

    public noReturnPredicate() {
        return false;
    }

    public onFilterUpgradable($event: boolean) {
        this.configStore.updateFilterUpgradable($event);
    }

    public onFilterUndeployed($event: boolean) {
        this.configStore.updateFilterUndeployed($event);
    }

    public trackConfigByName(index: number, item: Config) {
        return item.name;
    }

    onClickPaste(){
        let newConfig = navigator.clipboard.readText();
        newConfig.then((json)  => {
            console.log(json);
            this.editorService.configLoader.validateConfigJson(json)
                .subscribe(
                    v => {
                        // clean config
                        this.editorService.configStore.updatePastedConfig(JSON.parse(json));
                        this.router.navigate(
                            [this.editorService.serviceName, 'edit'],
                            { queryParams: { pasteConfig: true } });
                        // let  config = this.editorService.configLoader.getConfigFromFile(json);
                    }
                );
        })
    }
}
