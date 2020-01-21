import { AppConfigService } from '@app/config/app-config.service';
import { TestCase, TestCaseMap } from '@app/model/test-case';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';

import { ChangeDetectionStrategy, OnInit } from '@angular/core';
import { Component } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material';
import { ConfigData, ConfigWrapper } from '@app/model';
import { TEST_CASE_TAB_NAME } from '@app/model/test-case';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-editor-view',
    styleUrls: ['./editor-view.component.scss'],
    templateUrl: './editor-view.component.html',
})
export class EditorViewComponent implements OnInit {
    private readonly TEST_CASE_TAB_NAME = TEST_CASE_TAB_NAME;
    ngUnsubscribe = new Subject();
    bootstrapped$: Observable<string>;
    selectedConfig$: Observable<number>;

    testEnabled = false;
    sensorFieldsEnabled = false;
    testCaseEnabled = false;
    serviceName: string;
    configs$: Observable<ConfigWrapper<ConfigData>[]> = new Observable();
    schema$: Observable<any> = new Observable();
    selectedConfigIndex: number = undefined;
    fields: FormlyFieldConfig[] = [];

    onClickTestCase$: Subject<MatTabChangeEvent> = new Subject();
    selectedConfigName: string;

    testCaseMap: any = {};
    testCases: TestCase[] = [];

    routeType = 'edit';
    user$: Observable<string>;
    dynamicFieldsMap$: Observable<Map<string, string>>;
    configs: ConfigWrapper<ConfigData>[];
    testCaseMap$: Observable<TestCaseMap>;

    constructor(private store: Store<fromStore.State>, private config: AppConfigService, private formlyJsonschema: FormlyJsonschema) {
        this.store.select(fromStore.getServiceName).pipe(takeUntil(this.ngUnsubscribe)).subscribe(r => {
            this.serviceName = r
            this.testEnabled = this.config.getUiMetadata(r).testing.perConfigTestEnabled;
            this.sensorFieldsEnabled = this.config.getUiMetadata(r).enableSensorFields;
            this.testCaseEnabled = this.config.getUiMetadata(r).testing.testCaseEnabled;
        });
        this.bootstrapped$ = this.store.select(fromStore.getBootstrapped);
        this.selectedConfig$ = this.store.select(fromStore.getSelectedConfig);
        this.configs$ = this.store.select(fromStore.getConfigs);
        this.schema$ = this.store.select(fromStore.getSchema);
        this.user$ = this.store.select(fromStore.getCurrentUser);
        this.dynamicFieldsMap$ = this.store.select(fromStore.getDynamicFieldsMap);
        this.store.select(fromStore.getTestCaseMap).pipe(takeUntil(this.ngUnsubscribe)).subscribe(t => this.testCaseMap = t);
        this.testCaseMap$ = this.store.select(fromStore.getTestCaseMap);
    }

    ngOnInit() {
        this.schema$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(
            s => {
                this.fields = [this.formlyJsonschema.toFieldConfig(s.schema)];
                this.store.dispatch(new fromStore.UpdateDynamicFieldsMap(this.formlyJsonschema.dynamicFieldsMap));
        });

        this.configs$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(c => this.configs = c);

        this.selectedConfig$.subscribe(s => {
            this.selectedConfigName = this.configs[s].name;
            this.testCases = cloneDeep(this.testCaseMap[this.selectedConfigName]) || [];
            this.selectedConfigIndex = s;
        });

        this.testCaseMap$.pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(t => {
                this.testCases = cloneDeep(this.testCaseMap[this.selectedConfigName]) || []
            })
    }

    changeRoute(route) {
        this.store.dispatch(new fromStore.Go({
            path: route,
        }))
    }
}
