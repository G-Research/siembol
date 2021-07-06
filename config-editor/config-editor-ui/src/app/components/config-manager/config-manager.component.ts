import { animate, query, stagger, style, transition, trigger } from '@angular/animations';
import { CdkDrag, CdkDragDrop, CdkDropList } from '@angular/cdk/drag-drop';
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
import { AppConfigService } from '@app/services/app-config.service';
import { Type } from '@app/model/config-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-config-manager',
  styleUrls: ['./config-manager.component.scss'],
  templateUrl: './config-manager.component.html',
  animations: [
    trigger('list', [
      transition(':enter', [
        transition('* => *', []),
        query(
          ':enter',
          [
            style({ opacity: 0 }),
            stagger(10, [
              style({ transform: 'scale(0.8)', opacity: 0 }),
              animate('.6s cubic-bezier(.8,-0.6,0.7,1.5)', style({ transform: 'scale(1)', opacity: 1 })),
            ]),
          ],
          { optional: true }
        ),
      ]),
    ]),
  ],
})
export class ConfigManagerComponent implements OnInit, OnDestroy {
  @BlockUI() blockUI: NgBlockUI;
  allConfigs$: Observable<Config[]>;
  filteredConfigs$: Observable<Config[]>;
  deployment$: Observable<Deployment>;
  deployment: Deployment;
  configs: Config[];
  selectedConfig$: Observable<number>;
  selectedConfig: number;
  pullRequestPending$: Observable<PullRequestInfo>;
  releaseSubmitInFlight$: Observable<boolean>;
  searchTerm$: Observable<string>;
  filteredDeployment: Deployment;
  filteredDeployment$: Observable<Deployment>;
  filterMyConfigs$: Observable<boolean>;
  filterUndeployed$: Observable<boolean>;
  filterUpgradable$: Observable<boolean>;
  deploymentHistory$: Observable<FileHistory[]>;
  deploymentHistory;
  disableEditingFeatures: boolean;

  private ngUnsubscribe = new Subject();
  private filteredConfigs: Config[];
  private configStore: ConfigStoreService;
  private readonly PR_OPEN_MESSAGE = 'A pull request is already open';
  constructor(
    public dialog: MatDialog,
    private snackbar: PopupService,
    private editorService: EditorService,
    private router: Router,
    private configService: AppConfigService
  ) {
    this.disableEditingFeatures = editorService.metaDataMap.disableEditingFeatures;
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

    this.filteredConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => (this.filteredConfigs = s));

    this.filteredDeployment$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.filteredDeployment = cloneDeep(s);
    });

    this.deploymentHistory$
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(h => (this.deploymentHistory = { fileHistory: h }));
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSearch(searchTerm: string) {
    this.configStore.updateSearchTerm(searchTerm);
  }

  upgrade(index: number) {
    this.configStore.upgradeConfigInDeployment(index);
  }

  drop(event: CdkDragDrop<Config[]>) {
    if (event.container.id === 'deployment-list') {
      if (event.previousContainer.id === 'store-list') {
        this.configStore.addConfigToDeploymentInPosition(event.previousIndex, event.currentIndex);
      } else if (event.previousContainer.id === 'deployment-list' && event.currentIndex !== event.previousIndex) {
        this.configStore.moveConfigInDeployment(event.previousIndex, event.currentIndex);
      }
    }
  }

  onView(id: number, releaseId: number = undefined) {
    this.dialog.open(JsonViewerComponent, {
      data: {
        config1: releaseId === undefined ? undefined : this.filteredDeployment.configs[releaseId].configData,
        config2: this.filteredConfigs[id].configData,
      },
    });
  }

  onEdit(id: number) {
    this.router.navigate([this.editorService.serviceName, 'edit'], {
      queryParams: { configName: this.filteredConfigs[id].name },
    });
  }

  addToDeployment(id: number) {
    this.configStore.addConfigToDeployment(id);
  }

  onClone(id: number) {
    this.router.navigate([this.editorService.serviceName, 'edit'], {
      queryParams: { newConfig: true, cloneConfig: this.filteredConfigs[id].name },
    });
  }

  onRemove(id: number) {
    this.configStore.removeConfigFromDeployment(id);
  }

  onClickCreate() {
    this.router.navigate([this.editorService.serviceName, 'edit'], { queryParams: { newConfig: true } });
  }

  onDeploy() {
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

  onFilterMine($event: boolean) {
    this.configStore.updateFilterMyConfigs($event);
  }

  onSyncWithGit() {
    this.blockUI.start('loading store and deployments');
    this.configStore.reloadStoreAndDeployment().subscribe(() => {
      this.blockUI.stop();
    });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  duplicateItemCheck(item: CdkDrag<Config>, deployment: CdkDropList<Config[]>) {
    return deployment.data.find(d => d.name === item.data.name) === undefined ? true : false;
  }

  noReturnPredicate() {
    return false;
  }

  onFilterUpgradable($event: boolean) {
    this.configStore.updateFilterUpgradable($event);
  }

  onFilterUndeployed($event: boolean) {
    this.configStore.updateFilterUndeployed($event);
  }

  trackConfigByName(index: number, item: Config) {
    return item.name;
  }

  deleteConfigFromStore(index: number) {
    this.blockUI.start('deleting config');
    this.configStore.deleteConfig(this.filteredConfigs[index].name).subscribe(() => {
      this.blockUI.stop();
    });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  onClickPaste() {
    this.editorService.configStore.clipboardService.validateConfig(Type.CONFIG_TYPE).subscribe(() => {
      this.router.navigate([this.editorService.serviceName, 'edit'], { queryParams: { pasteConfig: true } });
    });
  }
}
