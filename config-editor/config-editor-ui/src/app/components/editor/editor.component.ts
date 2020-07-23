import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { AppConfigService } from '@app/config';
import { EditorService } from '@services/editor.service';
import { ConfigData, ConfigWrapper, SensorFields } from '@app/model';
import { TEST_CASE_TAB_NAME } from '@model/test-case';
import { UiMetadataMap } from '@model/ui-metadata-map';
import * as JsonPointer from '@app/ngx-formly/util/jsonpointer.functions';
import { PopupService } from '@app/popup.service';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import * as fromStore from 'app/store';
import { cloneDeep } from 'lodash';
import * as omitEmpty from 'omit-empty';
import { Observable, of, Subject } from 'rxjs';
import { filter, take, takeUntil } from 'rxjs/operators';
import { SubmitDialogComponent } from '..';
import { TestingDialogComponent } from '../testing/testing-dialog/testing-dialog.component';
import { Router } from '@angular/router';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-generic-editor',
    styleUrls: ['./editor.component.scss'],
    templateUrl: './editor.component.html',
})
export class EditorComponent implements OnInit, OnDestroy {
    private readonly TEST_CASE_TAB_NAME = TEST_CASE_TAB_NAME;
    public ngUnsubscribe = new Subject();
    public schema$: Observable<any>;
    public user$: Observable<string>;
    public configName: string;
    public selectedIndex: number;
    public configs: ConfigWrapper<ConfigData>[];
    public config: ConfigWrapper<ConfigData> = undefined;
    public configData: ConfigData = {};
    public schema: any = {};
    public enabled = 0;
    private metaDataMap: UiMetadataMap;
    public options: FormlyFormOptions = {};
    public form: FormGroup = new FormGroup({});

    private readonly NO_INPUT_MESSAGE = 'No data inputted to form';
    private readonly NO_NAME_MESSAGE = 'A name must be provided';
    private readonly UNIQUE_NAME_MESSAGE = 'Config name must be unique';
    private readonly SPACE_IN_NAME_MESSAGE = 'Config names cannot contain spaces';

    @Input() configs$: Observable<ConfigWrapper<ConfigData>[]>;
    @Input() selectedIndex$: Observable<number>;
    @Input() testEnabled: boolean;
    @Input() sensorFieldsEnabled: boolean;
    @Input() serviceName: string;
    @Input() fields: FormlyFieldConfig[];
    @Input() user: string;
    @Input() dynamicFieldsMap: Map<string, string>;
    @Input() onClickTestCase$: Observable<MatTabChangeEvent>;


    constructor(public store: Store<fromStore.State>, public dialog: MatDialog, public snackbar: PopupService,
        private appConfigService: AppConfigService, private editorService: EditorService, private router: Router) {
        }

        ngOnInit() {
            this.metaDataMap = this.appConfigService.getUiMetadata(this.serviceName);

            this.configs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(r => {
                this.configs = <ConfigWrapper<ConfigData>[]>r;
                if (this.config && this.selectedIndex) {
                    this.config = this.configs[this.selectedIndex];
                }
            });

            this.selectedIndex$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(r => {
                this.store.dispatch(new fromStore.SelectDataSource(undefined));
                if (this.config && this.configData) {
                    this.pushRuleUpdateToState();
                }
                this.selectedIndex = r;
                this.config = this.configs[r];
                if (this.config === undefined) {
                    this.configData = {};
                } else {
                    this.configData = cloneDeep(this.config.configData);
                    this.configName = this.config.name;
                }
                this.options.formState = {
                    mainModel: this.configData,
                };
            });

            this.onClickTestCase$.pipe(
                takeUntil(this.ngUnsubscribe),
                filter(f => f.tab.textLabel === TEST_CASE_TAB_NAME)
            ).subscribe(s => {
                this.pushRuleUpdateToState();
            });
    }

    ngOnDestroy() {
        if (this.config) {
            this.store.select(fromStore.getServiceName).pipe(take(1))
                .subscribe(serviceName => {
                        this.pushRuleUpdateToState();
                })
        }
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    private cleanConfigData(configData: ConfigData): ConfigData {
        let cfg = this.editorService.configWrapper.produceOrderedJson(configData, '/');
        cfg = omitEmpty(cfg);
        return cfg;
    }

    pushRuleUpdateToState(): ConfigWrapper<ConfigData> {
        const configToUpdate = cloneDeep(this.config);
        configToUpdate.configData = cloneDeep(this.configData);
        if (this.config.isNew) {
            configToUpdate.configData[this.metaDataMap.name] = configToUpdate.name = this.configName;
            configToUpdate.configData[this.metaDataMap.version] = configToUpdate.version = 0;
            configToUpdate.configData[this.metaDataMap.author] = configToUpdate.author = this.user;
        } else {
            configToUpdate.configData[this.metaDataMap.name] = configToUpdate.name = this.config.name;
            configToUpdate.configData[this.metaDataMap.version] = configToUpdate.version = this.config.version;
            configToUpdate.configData[this.metaDataMap.author] = configToUpdate.author = this.config.author;
        }
        configToUpdate.description = configToUpdate.configData[this.metaDataMap.description];
        configToUpdate.configData = this.cleanConfigData(configToUpdate.configData);
        // check if rule has been changed, mark unsaved
        if (JSON.stringify(cloneDeep(this.config.configData)) !== 
            JSON.stringify(configToUpdate.configData)) { 
            configToUpdate.savedInBackend = false;
        }
        const newConfigs = Object.assign(this.configs.slice(), {
            [this.selectedIndex]: configToUpdate,
        });
        this.store.dispatch(new fromStore.UpdateConfigs(newConfigs));

        return configToUpdate;
    }

    onSubmit() {
        const config = this.pushRuleUpdateToState();

        if (!config.configData) {
            this.snackbar.openNotification(this.NO_INPUT_MESSAGE);

            return;
        }

        if (config.isNew) {
            if (this.configName === undefined || this.configName === '') {
                this.snackbar.openNotification(this.NO_NAME_MESSAGE);

                return;
            }

            if (this.configs.find(f => config !== f && f.configData && f.name === this.configName) !== undefined) {
                this.snackbar.openNotification(this.UNIQUE_NAME_MESSAGE);

                return;
            }

            if (this.configName.includes(' ')) {
                this.snackbar.openNotification(this.SPACE_IN_NAME_MESSAGE);
            }
        }

        this.dialog.open(SubmitDialogComponent, {
            data: {
                ...config,
                name: this.configName,
            },
        }).afterClosed().subscribe((submittedData: ConfigData) => {
            if (submittedData) {
                this.config = config;
                if (config.isNew) {
                    this.store.dispatch(new fromStore.SubmitNewConfig(config));
                    this.router.navigate([this.serviceName]);
                } else {
                    this.store.dispatch(new fromStore.SubmitConfigEdit(config));
                }
            }
        });
    }

    public onClone() {
        const newConfig: ConfigWrapper<ConfigData> = {
            isNew: true,
            configData: Object.assign({}, cloneDeep(this.config.configData), {
                [this.metaDataMap.name]: `${this.config.name}_clone`,
                [this.metaDataMap.version]: 0,
            }),
            savedInBackend: false,
            name: `${this.config.name}_clone`,
            author: this.user,
            version: 0,
            description: this.config.description,
        };

        this.store.dispatch(new fromStore.AddConfig(newConfig));
    }

    public onTest() {
        this.store.dispatch(new fromStore.Go({
            path: [this.serviceName, 'test', this.selectedIndex],
        }));
        const currentConfig = this.pushRuleUpdateToState().configData;
        this.dialog.open(TestingDialogComponent, {
            data: {
                configDto: currentConfig,
                singleConfig: true,
            },
        });
    }

    public onSelectDataSource(dataSource: string) {
        this.store.dispatch(new fromStore.SelectDataSource(dataSource));
    }
}
