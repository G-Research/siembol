import { animate, query, stagger, style, transition, trigger } from '@angular/animations';
import {
    CdkDrag,
    CdkDragDrop,
    CdkDropList,
    copyArrayItem,
    moveItemInArray,
    transferArrayItem,
} from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, ConfigWrapper, Deployment, PullRequestInfo, SubmitStatus } from '@app/model';
import { PopupService } from '@app/popup.service';
import { Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { skip, take, takeUntil } from 'rxjs/operators';
import { DeployDialogComponent } from '../deploy-dialog/deploy-dialog.component';
import { JsonViewerComponent } from '../json-viewer/json-viewer.component';

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
                        style({transform: 'scale(1)', opacity: 1 })),
                    ]),
                ], {optional: true}),
            ]),
        ]),
    ],
})
export class ConfigManagerComponent implements OnInit, OnDestroy {
    private ngUnsubscribe = new Subject();
    public allConfigs$: Observable<ConfigWrapper<ConfigData>[]>;
    public filteredConfigs$: Observable<ConfigWrapper<ConfigData>[]>;
    public deployment$: Observable<Deployment<ConfigWrapper<ConfigData>>>;
    public deployment: Deployment<ConfigWrapper<ConfigData>>;
    public configs: ConfigWrapper<ConfigData>[];
    public selectedConfig$: Observable<number>;
    public selectedConfig: number;
    public pullRequestPending$: Observable<PullRequestInfo>;
    public releaseStatus$: Observable<SubmitStatus>;
    public searchTerm$: Observable<string>;
    public filteredDeployment: Deployment<ConfigWrapper<ConfigData>>;
    public filteredDeployment$: Observable<Deployment<ConfigWrapper<ConfigData>>>;
    private filteredConfigs: ConfigWrapper<ConfigData>[];
    private serviceName: string;
    public filterMyConfigs$: Observable<boolean>;
    public filterUndeployed$: Observable<boolean>;
    public filterUpgradable$: Observable<boolean>;
    public deploymentHistory;

    private readonly UNSAVED_ITEM_ALERT = 'Unsaved item! Item must be submitted to the store before it can be deployed!';
    private readonly PR_OPEN_MESSAGE = 'A pull request is already open';

    constructor(private store: Store<fromStore.State>, public dialog: MatDialog, private snackbar: PopupService,
        private editorService: EditorService) {
        this.allConfigs$ = this.store.select(fromStore.getConfigs);
        this.filteredConfigs$ = this.store.select(fromStore.getConfigsFilteredBySearchTerm);
        this.selectedConfig$ = this.store.select(fromStore.getSelectedConfig);
        this.pullRequestPending$ = this.store.select(fromStore.getPullRequestPending);
        this.releaseStatus$ = this.store.select(fromStore.getSubmitReleaseStatus);
        this.deployment$ = this.store.select(fromStore.getStoredDeployment);
        this.searchTerm$ = this.store.select(fromStore.getSearchTerm);
        this.filteredDeployment$ = this.store.select(fromStore.getDeploymentFilteredBySearchTerm);
        this.filterMyConfigs$ = this.store.select(fromStore.getFilterMyConfigs);
        this.filterUndeployed$ = this.store.select(fromStore.getFilterUndeployed);
        this.filterUpgradable$ = this.store.select(fromStore.getFilterUpgradable);
        this.store.select(fromStore.getDeploymentHistory).pipe(take(1))
            .subscribe(h => this.deploymentHistory = {fileHistory: h});
    }

    ngOnInit() {
        this.selectedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => this.selectedConfig = s);
        this.deployment$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
            this.deployment = cloneDeep(s);
        });
        this.allConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(r => {
            this.configs = cloneDeep(r);
        });
        this.store.select(fromStore.getServiceName).pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(name => this.serviceName = name);

        this.filteredConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => this.filteredConfigs = s);
        this.filteredDeployment$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
            this.filteredDeployment = cloneDeep(s);
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    public onSearch(searchTerm: string) {
        this.store.dispatch(new fromStore.Go({
            extras: {
                queryParamsHandling: 'merge',
            },
            path: [],
            query: { search: searchTerm },
        }));
    }

    public upgrade(index: number) {
        const originalIndex = this.deployment.configs
            .findIndex(d => d.name === this.filteredDeployment.configs[index].name);
        const items = Object.assign(this.deployment.configs.slice(), {
            [originalIndex]: this.configs.find(c => c.name === this.deployment.configs[originalIndex].name),
        });
        this.deployment.configs = items;
        this.store.dispatch(new fromStore.UpdateDeployment(this.deployment));
    }

    public drop(event: CdkDragDrop<ConfigWrapper<ConfigData>[]>) {
        if (event.container.id === 'deployment-list' && event.previousContainer.id === 'store-list' && !event.item.data.savedInBackend) {
            alert(this.UNSAVED_ITEM_ALERT);

            return;
        }

        if (event.container.id === 'deployment-list') {
            if (event.previousContainer.id === 'store-list') {
                // In the case that the list has been filtered select the appropriate item to copy over
                const index = this.configs.findIndex(e => e.name === this.filteredConfigs[event.previousIndex].name);
                copyArrayItem(event.previousContainer.data,
                    event.container.data,
                    index,
                    event.currentIndex
                );
            } else if (event.previousContainer.id === 'deployment-list') {
                const prevIndex = this.deployment.configs.findIndex(e =>
                    e.name === this.filteredDeployment.configs[event.previousIndex].name);
                const currIndex = this.deployment.configs.findIndex(e =>
                    e.name === this.filteredDeployment.configs[event.currentIndex].name);
                moveItemInArray(event.container.data, prevIndex, currIndex);
            }
            this.store.dispatch(new fromStore.UpdateDeployment(cloneDeep(this.deployment)));
        }
    }

    public onView(id: number, releaseId: number = undefined) {
        this.dialog.open(JsonViewerComponent, {
            data: {
                config1: releaseId === undefined ? undefined
                    : this.editorService.configWrapper
                        .unwrapOptionalsFromArrays(cloneDeep(this.filteredDeployment.configs[releaseId].configData)),
                config2: this.editorService.configWrapper.unwrapConfig(cloneDeep(this.filteredConfigs[id].configData)),
            },
        });
    }

    public onEdit(id: number) {
        const index = this.configs.findIndex(r => r.name === this.filteredConfigs[id].name);
        if (index > -1) {
            this.store.dispatch(new fromStore.Go({
                path: [this.serviceName, 'edit', index],
            }));
        }
    }

    public addToDeployment(id: number) {
        const item: ConfigWrapper<ConfigData> = cloneDeep(this.configs.find(r => r.name === this.filteredConfigs[id].name));
        if (item !== undefined) {
            if (!item.savedInBackend) {
                alert(this.UNSAVED_ITEM_ALERT);

                return;
            }
            if (this.deployment.configs.find(r => r.name === item.name) === undefined) {
                const updatedDeployment: Deployment<ConfigWrapper<ConfigData>> = cloneDeep(this.deployment);
                const orderedItem: ConfigData = this.editorService.configWrapper.produceOrderedJson(item.configData, '/');
                item.configData = this.editorService.configWrapper.unwrapConfig(orderedItem);
                updatedDeployment.configs.push(item);
                this.store.dispatch(new fromStore.UpdateDeployment(updatedDeployment));
            }
        }
    }

    public onRemove(id: number) {
        const index = this.deployment.configs
            .findIndex(r => r.name === this.filteredDeployment.configs[id].name);

        transferArrayItem(this.deployment.configs, [], index, 0);
        this.store.dispatch(new fromStore.UpdateDeployment(cloneDeep(this.deployment)));
    }

    public onClickCreate() {
        this.store.dispatch(new fromStore.AddConfig({
            isNew: true,
            configData: {},
            savedInBackend: false,
            name: `new_entry_${this.configs.length}`,
            version: 0,
            description: 'no description',
            author: 'no author',
        }));
    }

    public onDeploy() {
        this.store.dispatch(new fromStore.LoadPullRequestStatus());
        this.pullRequestPending$.pipe(skip(1), take(1)).subscribe(a => {
            if (!a.pull_request_pending) {
                const dialogRef = this.dialog.open(DeployDialogComponent, {
                    data: cloneDeep(this.deployment),
                });
                dialogRef.afterClosed().subscribe((results: Deployment<ConfigWrapper<ConfigData>>) => {
                    if (results && results.configs.length > 0) {
                        if (results.deploymentVersion >= 0) {
                            this.store.dispatch(new fromStore.SubmitRelease(results));
                        }
                    }
                });
            } else {
                this.snackbar.openNotification(this.PR_OPEN_MESSAGE);
            }
        });
    }

    public onFilterMine($event: boolean) {
        this.store.dispatch(new fromStore.FilterMyConfigs($event));
    }

    public onRefreshPrStatus() {
        this.store.dispatch(new fromStore.LoadPullRequestStatus());
    }

    public duplicateItemCheck(item: CdkDrag<ConfigWrapper<ConfigData>>, deployment: CdkDropList<ConfigWrapper<ConfigData>[]>) {
        return deployment.data.find(d => d.name === item.data.name) === undefined
            ? true : false;
    }

    public noReturnPredicate() {
        return false;
    }

    public onFilterUpgradable($event: boolean) {
        this.store.dispatch(new fromStore.FilterUpgradable($event));
    }

    public onFilterUndeployed($event: boolean) {
        this.store.dispatch(new fromStore.FilterUndeployed($event));
    }
}
