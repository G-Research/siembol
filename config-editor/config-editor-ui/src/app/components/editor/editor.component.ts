import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog, MatTabChangeEvent } from '@angular/material';
import { AppConfigService } from '@app/config';
import { EditorService } from '@app/editor.service';
import { ConfigData, ConfigWrapper, SensorFields } from '@app/model';
import { TEST_CASE_TAB_NAME } from '@app/model/test-case';
import { UiMetadataMap } from '@app/model/ui-metadata-map';
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
    public sensors$: Observable<SensorFields[]> = of([]);
    public selectedSensor$: Observable<string>;
    private metaDataMap: UiMetadataMap;
    public options: FormlyFormOptions = {};
    public sensors: SensorFields[] = [];

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
        private appConfigService: AppConfigService, private editorService: EditorService) {
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
                }
                this.configName = this.config.name;
                this.options.formState = {
                    mainModel: this.configData,
                    sensorFields: this.sensors,
                };
            });

            this.store.select(fromStore.getSensorListFromDataSource).pipe(takeUntil(this.ngUnsubscribe)).subscribe(
                s => this.sensors = s
            );

            this.onClickTestCase$.pipe(
                takeUntil(this.ngUnsubscribe),
                filter(f => f.tab.textLabel === TEST_CASE_TAB_NAME)
            ).subscribe(s => {
                this.pushRuleUpdateToState();
            });

            if (this.sensorFieldsEnabled) {
                this.sensors$ = this.store.select(fromStore.getSensorFields);
                this.store.dispatch(new fromStore.LoadCentrifugeFields());
                this.selectedSensor$ = this.store.select(fromStore.getDataSource);
                this.selectedSensor$.subscribe(s => this.options.formState.sensorFields = this.sensors)
            }
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
        let cfg = this.removeFieldsWhichShouldBeHidden(this.dynamicFieldsMap, configData);
        cfg = this.editorService.getLoader(this.serviceName).produceOrderedJson(cfg, '/');
        // recursively removes null, undefined, empty objects, empty arrays from the object
        cfg = omitEmpty(cfg);

        return cfg;
    }

    pushRuleUpdateToState(): ConfigWrapper<ConfigData> {
        const cleanedConfigData = this.cleanConfigData(this.configData);

        const configToUpdate = cloneDeep(this.config);
        configToUpdate.configData = cloneDeep(cleanedConfigData);
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

        // check if rule has been changed, mark unsaved
        if (JSON.stringify(this.config.configData) !== JSON.stringify(configToUpdate.configData)) {
            configToUpdate.savedInBackend = false;
        }
        const newConfigs = Object.assign(this.configs.slice(), {
            [this.selectedIndex]: configToUpdate,
        });
        this.store.dispatch(new fromStore.UpdateConfigs(newConfigs));

        return configToUpdate;
    }

    private removeFieldsWhichShouldBeHidden(functionsMap: Map<string, string>, cfg): ConfigData {
        const data = cloneDeep(cfg);
        const functionsMapKeys = [];
        const functionsMapItr = functionsMap.keys();
        for (let i = 0; i < functionsMap.size; ++i) {
            functionsMapKeys.push(functionsMapItr.next().value);
        }

        for (const stringPath of functionsMapKeys) {
            const path = JsonPointer.JsonPointer.parse(stringPath);
            // find the lengths of arrays of objects so they can be iterated through with conditional function
            const arrayIndices = [];
            for (let j = 0; j < path.length; j++) {
                // in generic path '-' corresponds to an array index
                if (path[j] === '-') {
                    const obj = JsonPointer.JsonPointer.get(data, path.slice(0, j));
                    if (obj !== undefined) {
                        arrayIndices.push(obj.length - 1);
                    }
                }
            }

            if (arrayIndices.length === 0) {
                this.removeFromDataIfFuncFalse(data, {}, functionsMap, stringPath);
            }
            // TODO replace this with generic implementation where arrays of arrays is possible in JSON structure
            for (let j = 0; j <= arrayIndices[0]; j++) {
                this.removeFromDataIfFuncFalse(data, {parent: { parent: { key: j}, key: j } }, functionsMap, stringPath);
            }
        }

        return data;
    }

    private removeFromDataIfFuncFalse(data, field, functionsMap: Map<string, string>, stringPath: string) {
        try {
            const func = functionsMap.get(stringPath);
            const dynFunc = new Function('model', 'localfield', 'field', func);
            if (dynFunc(data, null, field)) {
                let indexedPointer;
                if (stringPath.includes('-')) {
                    indexedPointer = JsonPointer.JsonPointer.toIndexedPointer(stringPath, [field.parent.parent.key]);
                } else {
                    indexedPointer = stringPath;
                }
                JsonPointer.JsonPointer.remove(data, indexedPointer);
            }

        } catch {
            console.warn('Something went wrong with condition evaluation when cleaning form', stringPath);
        }
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

        this.form = new FormGroup({});
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
