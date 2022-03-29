import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { Config, Release, PullRequestInfo } from '@app/model';
import { PopupService } from '@app/services/popup.service';
import { cloneDeep } from 'lodash';
import { Observable, Subject, first } from 'rxjs';
import { skip, take, takeUntil } from 'rxjs/operators';
import { ReleaseDialogComponent } from '../release-dialog/release-dialog.component';
import { JsonViewerComponent } from '../json-viewer/json-viewer.component';
import { FileHistory } from '../../model';
import { ConfigStoreService } from '../../services/store/config-store.service';
import { Router, Params } from '@angular/router';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { AppConfigService } from '@app/services/app-config.service';
import { CheckboxEvent, ConfigManagerRow, Filters, Importers, ServiceFilters, Type } from '@app/model/config-model';
import { ImporterDialogComponent } from '../importer-dialog/importer-dialog.component';
import { CloneDialogComponent } from '../clone-dialog/clone-dialog.component';
import { configManagerColumns } from './columns';
import { RowDragEvent, GridSizeChangedEvent, RowNode, GridApi, GetRowIdFunc } from '@ag-grid-community/core';
import { FilterConfig } from '@app/model/ui-metadata-map';

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
  releaseHistory$: Observable<FileHistory[]>;
  rowData$: Observable<ConfigManagerRow[]>;
  isAnyFilterPresent$: Observable<boolean>;
  isAnyFilterPresent: boolean;
  serviceFilterConfig: FilterConfig;
  serviceFilters$: Observable<ServiceFilters>;
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
    suppressMovableColumns: true,
    suppressMoveWhenRowDragging: true,
    rowDragManaged: true,
    rowSelection: 'single',
    suppressClickEdit: true,
    rowHeight: 50,
    animateRows:true,
    onRowDragEnter: (event: RowDragEvent) => {
      this.rowMoveStartIndex = event.node.rowIndex;
    },
    onRowDragEnd: (event: RowDragEvent) => {
      this.drop(event);
    },
    onGridSizeChanged: (event: GridSizeChangedEvent) => {
      event.api.sizeColumnsToFit();
    }, 
    isExternalFilterPresent: () => this.isAnyFilterPresent,
    doesExternalFilterPass: (node: RowNode) => node.data.isFiltered,
  };
  api: GridApi;
  countChangesInRelease$ : Observable<number>;
  currentFilters: Filters[] = [];
  currentSearchTerm: string;
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

    this.releaseHistory$ = this.configStore.releaseHistory$;
    this.importers$ = this.configStore.importers$;
    this.useImporters = this.configService.useImporters;

    this.rowData$ = this.configStore.configManagerRowData$;
    this.isAnyFilterPresent$ = this.configStore.isAnyFilterPresent$;
    this.countChangesInRelease$ = this.configStore.countChangesInRelease$;
    this.serviceFilters$ = this.configStore.serviceFilters$;
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
    this.isAnyFilterPresent$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.isAnyFilterPresent = cloneDeep(s);
    });
    this.configStore.serviceFilterConfig$.pipe(first()).subscribe(s => {
      this.serviceFilterConfig = cloneDeep(s);
    });
    this.searchTerm$.pipe(first()).subscribe(s => {
      this.currentSearchTerm = s;
    });
  }

  onGridReady(params: any) {
    this.api = params.api;
    this.searchTerm$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.api.setQuickFilter(s);
    });
    this.serviceFilters$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(() => {
      this.api.onFilterChanged();
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSearch(searchTerm: string) {
    this.currentSearchTerm = searchTerm;
    this.router.navigate([this.editorService.serviceName], {
      queryParams: this.getQueryParams(),
    });
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

  onClickCheckbox(event: CheckboxEvent) {
    this.setCurrentFilters(event);
    this.router.navigate([this.editorService.serviceName], {
      queryParams: this.getQueryParams(),
    });
  }

  getQueryParams(): Params {
    const params = new URLSearchParams();
    for (const filter of this.currentFilters) {
      if (filter.groupName in params) {
        params[filter.groupName].push(filter.filterName);
      } else {
        params[filter.groupName] = [filter.filterName];
      }
    }
    if (this.currentSearchTerm) {
      params["searchTerm"] = this.currentSearchTerm;
    }
    return params;
  }
  
  setCurrentFilters(event: CheckboxEvent) {
    if (event.checked === true) {
      this.currentFilters.push({
        groupName: event.groupName,
        filterName: event.filterName,
      });
    } else {
      this.currentFilters = this.currentFilters.filter(
        obj => obj.groupName !== event.groupName || obj.filterName !== event.filterName
      );
    }
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

  getRowId: GetRowIdFunc = function (params) {
    return params.data.config_name;
  };
}
