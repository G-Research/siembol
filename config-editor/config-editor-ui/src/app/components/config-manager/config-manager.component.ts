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
import { CheckboxEvent, ConfigManagerRow, Importers, Type } from '@app/model/config-model';
import { ImporterDialogComponent } from '../importer-dialog/importer-dialog.component';
import { CloneDialogComponent } from '../clone-dialog/clone-dialog.component';
import { configManagerColumns } from './columns';
import { GetRowNodeIdFunc, RowDragEvent, GridSizeChangedEvent, RowNode } from '@ag-grid-community/core';
import { CheckboxConfig } from '@app/model/ui-metadata-map';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-config-manager',
  styleUrls: ['./config-manager.component.scss'],
  templateUrl: './config-manager.component.html',
})
export class ConfigManagerComponent implements OnInit, OnDestroy {
  @BlockUI() blockUI: NgBlockUI;
  allConfigs$: Observable<Config[]>;
  configs: Config[];
  release$: Observable<Release>;
  release: Release;
  pullRequestPending$: Observable<PullRequestInfo>;
  releaseSubmitInFlight$: Observable<boolean>;
  searchTerm$: Observable<string>;
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
  context: any;
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
    onGridReady: (params: any) => {
      this.api = params.api;
    },
    isExternalFilterPresent: this.isExternalFilterPresent.bind(this),
   doesExternalFilterPass: this.doesExternalFilterPass.bind(this),
  };
  api;
  checkboxFilters: CheckboxConfig;
  private countChangesInRelease$ : Observable<number>;
  private rowMoveStartIndex: number;

  private ngUnsubscribe = new Subject<void>();
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
    this.allConfigs$ = this.configStore.sortedConfigs$;

    this.pullRequestPending$ = this.configStore.pullRequestPending$;
    this.releaseSubmitInFlight$ = this.configStore.releaseSubmitInFlight$;
    this.release$ = this.configStore.release$;

    this.searchTerm$ = this.configStore.searchTerm$;
    this.filterMyConfigs$ = this.configStore.filterMyConfigs$;

    this.filterUnreleased$ = this.configStore.filterUnreleased$;
    this.filterUpgradable$ = this.configStore.filterUpgradable$;

    this.releaseHistory$ = this.configStore.releaseHistory$;
    this.importers$ = this.configStore.importers$;
    this.useImporters = this.configService.useImporters;

    this.rowData$ = this.configStore.configManagerRowData$;
    this.countChangesInRelease$ = this.configStore.countChangesInRelease$;

    this.checkboxFilters = this.editorService.metaDataMap.checkboxes;
  }

  ngOnInit() {
    this.release$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.release = cloneDeep(s);
    });
    this.allConfigs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.configs = cloneDeep(s);
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
    this.api.setQuickFilter(
      searchTerm
    );
  }

  upgrade(name: string) {
    this.configStore.upgradeConfigInRelease(name);
    this.configStore.incrementChangesInRelease();
  }

  drop(event: RowDragEvent) {
    if (this.rowMoveStartIndex !== event.node.rowIndex) {
      this.configStore.moveConfigInRelease(event.node.id, event.node.rowIndex);
      this.configStore.incrementChangesInRelease();
    } 
  }

  onView(name: string) {
    this.dialog.open(JsonViewerComponent, {
      data: {
        config1: undefined, 
        config2: this.configs.find(c => c.name === name).configData,
      },
    });
  }

  onViewDiff(name: string) {
    this.dialog.open(JsonViewerComponent, {
      data: {
        config1: this.release.configs.find(r => r.name === name).configData,
        config2: this.configs.find(c => c.name === name).configData,
      },
    });
  }

  onEdit(name: string) {
    this.router.navigate([this.editorService.serviceName, 'edit'], {
      queryParams: { configName: name },
    });
  }

  addToRelease(name: string) {
    this.configStore.addConfigToRelease(name);
    this.configStore.incrementChangesInRelease();
  }

  onClone(name: string) {
    this.dialog.open(CloneDialogComponent, 
      { data: name, disableClose: true });
  }

  onRemove(name: string) {
    this.configStore.removeConfigFromRelease(name);
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
    this.api.onFilterChanged();
  }

  onClickCheckbox(event: CheckboxEvent) {
    this.configStore.updateCheckboxFilters(event);
    this.api.onFilterChanged();
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
    this.api.onFilterChanged();
  }

  onFilterUnreleased($event: boolean) {
    this.configStore.updateFilterUnreleased($event);
    this.api.onFilterChanged();
  }

  deleteConfigFromStore(name: string) {
    this.blockUI.start('deleting config');
    this.configStore.deleteConfig(name).subscribe(() => {
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

  isExternalFilterPresent(): boolean {
    return this.configStore.isExternalFilterPresent();
  }

  doesExternalFilterPass(node: RowNode): boolean {
    return this.configStore.doesExternalFilterPass(node);
  }
}
