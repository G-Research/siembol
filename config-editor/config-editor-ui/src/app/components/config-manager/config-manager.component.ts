import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { Config, Release, PullRequestInfo } from '@app/model';
import { PopupService } from '@app/services/popup.service';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { skip, take, takeUntil } from 'rxjs/operators';
import { ReleaseDialogComponent } from '../release-dialog/release-dialog.component';
import { JsonViewerComponent } from '../json-viewer/json-viewer.component';
import { FileHistory } from '../../model';
import { ConfigStoreService } from '../../services/store/config-store.service';
import { Router } from '@angular/router';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { AppConfigService } from '@app/services/app-config.service';
import { ConfigManagerRow, Importers, Type } from '@app/model/config-model';
import { ImporterDialogComponent } from '../importer-dialog/importer-dialog.component';
import { CloneDialogComponent } from '../clone-dialog/clone-dialog.component';
import { configManagerColumns } from './columns';
import { GetRowNodeIdFunc, RowDragEvent, GridSizeChangedEvent } from '@ag-grid-community/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-config-manager',
  styleUrls: ['./config-manager.component.scss'],
  templateUrl: './config-manager.component.html',
})
export class ConfigManagerComponent implements OnInit, OnDestroy {
  @BlockUI() blockUI: NgBlockUI;
  allConfigs$: Observable<Config[]>;
  filteredConfigs$: Observable<Config[]>;
  release$: Observable<Release>;
  release: Release;
  selectedConfig$: Observable<number>;
  selectedConfig: number;
  pullRequestPending$: Observable<PullRequestInfo>;
  releaseSubmitInFlight$: Observable<boolean>;
  searchTerm$: Observable<string>;
  filteredRelease: Release;
  filteredRelease$: Observable<Release>;
  filterMyConfigs$: Observable<boolean>;
  filterUnreleased$: Observable<boolean>;
  filterUpgradable$: Observable<boolean>;
  releaseHistory$: Observable<FileHistory[]>;
  rowData$: Observable<ConfigManagerRow[]>;
  releaseHistory;
  disableEditingFeatures: boolean;
  importers$: Observable<Importers>;
  importers: Importers;
  useImporters: boolean;
  columnDefs = configManagerColumns;
  defaultColDef = {
    flex: 1,
    autoHeight: true,
  };
  context;
  gridOptions = {
    tooltipShowDelay: 100,
    suppressMoveWhenRowDragging: true,
    rowDragManaged:true,
    rowSelection: 'single',
    suppressClickEdit: true,
    rowHeight: 50,
    onRowDragEnter: (event: RowDragEvent) => {
      this.rowMoveStartIndex = event.node.rowIndex;
    },
    onRowDragEnd: (event: RowDragEvent) => {
      this.drop(event);
    },
    onGridSizeChanged: (event: GridSizeChangedEvent) => {
      event.api.sizeColumnsToFit();
    }, 
  }
  private countChangesInRelease$ : Observable<number>;
  private rowMoveStartIndex: number;

  private ngUnsubscribe = new Subject<void>();
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
    this.context = { componentParent: this };

    this.disableEditingFeatures = editorService.metaDataMap.disableEditingFeatures;
    this.configStore = editorService.configStore;
    this.allConfigs$ = this.configStore.allConfigs$;

    this.filteredConfigs$ = this.configStore.filteredConfigs$;

    this.pullRequestPending$ = this.configStore.pullRequestPending$;
    this.releaseSubmitInFlight$ = this.configStore.releaseSubmitInFlight$;
    this.release$ = this.configStore.release$;

    this.searchTerm$ = this.configStore.searchTerm$;
    this.filteredRelease$ = this.configStore.filteredRelease$;
    this.filterMyConfigs$ = this.configStore.filterMyConfigs$;

    this.filterUnreleased$ = this.configStore.filterUnreleased$;
    this.filterUpgradable$ = this.configStore.filterUpgradable$;

    this.releaseHistory$ = this.configStore.releaseHistory$;
    this.importers$ = this.configStore.importers$;
    this.useImporters = this.configService.useImporters;

    this.rowData$ = this.configStore.configManagerRowData$;
    this.countChangesInRelease$ = this.configStore.countChangesInRelease$;
  }

  ngOnInit() {
    this.release$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.release = cloneDeep(s);
    });

    this.filteredConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => (this.filteredConfigs = s));

    this.filteredRelease$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.filteredRelease = cloneDeep(s);
    });

    this.releaseHistory$
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(h => (this.releaseHistory = { fileHistory: h }));

    this.importers$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(i => {
      this.importers = i;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSearch(searchTerm: string) {
    this.configStore.updateSearchTerm(searchTerm);
  }

  upgrade(index: number) {
    this.configStore.upgradeConfigInRelease(index);
    this.configStore.incrementChangesInRelease();
  }

  drop(event: RowDragEvent) {
    if (this.rowMoveStartIndex !== event.node.rowIndex) {
      this.configStore.moveConfigInRelease(event.node.id, event.node.rowIndex);
      this.configStore.incrementChangesInRelease();
    } 
  }

  onView(id: number) {
    this.dialog.open(JsonViewerComponent, {
      data: {
        config1: id >= this.filteredRelease.configs.length? 
          undefined : 
          this.filteredRelease.configs[id].configData,
        config2: this.filteredConfigs[id].configData,
      },
    });
  }

  onEdit(id: number) {
    this.router.navigate([this.editorService.serviceName, 'edit'], {
      queryParams: { configName: this.filteredConfigs[id].name },
    });
  }

  addToRelease(id: number) {
    this.configStore.addConfigToRelease(id);
    this.configStore.incrementChangesInRelease();
  }

  onClone(id: number) {
    this.dialog.open(CloneDialogComponent, 
      { data: this.filteredConfigs[id].name, disableClose: true });
  }

  onRemove(id: number) {
    this.configStore.removeConfigFromRelease(id);
    this.configStore.incrementChangesInRelease();
  }

  onClickCreate() {
    this.router.navigate([this.editorService.serviceName, 'edit'], { queryParams: { newConfig: true } });
  }

  onRelease() {
    this.configStore.loadPullRequestStatus();
    this.pullRequestPending$.pipe(skip(1), take(1)).subscribe(a => {
      if (!a.pull_request_pending) {
        const dialogRef = this.dialog.open(ReleaseDialogComponent, {
          data: cloneDeep(this.release),
          maxHeight: '90vh',
        });
        dialogRef.afterClosed().subscribe((results: Release) => {
          if (results && results.configs.length > 0) {
            if (results.releaseVersion >= 0) {
              this.configStore.submitRelease(results);
              this.configStore.resetChangesInRelease();
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
    this.blockUI.start('loading store and releases');
    this.configStore.reloadStoreAndRelease().subscribe(() => {
      this.configStore.resetChangesInRelease();
      this.blockUI.stop();
    });
    setTimeout(() => {
      this.blockUI.stop();
    }, this.configService.blockingTimeout);
  }

  onFilterUpgradable($event: boolean) {
    this.configStore.updateFilterUpgradable($event);
  }

  onFilterUnreleased($event: boolean) {
    this.configStore.updateFilterUnreleased($event);
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

  onClickImport(index: number) {
    const data = this.importers.config_importers[index];
    const dialogRef = this.dialog.open(ImporterDialogComponent, { data });   
    dialogRef.afterClosed().subscribe(success => {
      if (success) {
        this.router.navigate([this.editorService.serviceName, 'edit'], { queryParams: { pasteConfig: true } });
      }
    });
  }

  getRowNodeId: GetRowNodeIdFunc = function (data) {
    return data.config_name;
  };
}
