import { AppConfigService } from '@app/config/app-config.service';
import { TestCase, TestCaseMap } from '@app/model/test-case';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';
import { ChangeDetectionStrategy, OnInit } from '@angular/core';
import { Component } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { ConfigData, ConfigWrapper } from '@app/model';
import { TEST_CASE_TAB_NAME, TESTING_TAB_NAME } from '@app/model/test-case';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { JSONSchema7 } from 'json-schema';
import { EditorService } from '@app/services/editor.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-editor-view',
  styleUrls: ['./editor-view.component.scss'],
  templateUrl: './editor-view.component.html'
})
export class EditorViewComponent implements OnInit {
  readonly TEST_CASE_TAB_NAME = TEST_CASE_TAB_NAME;
  readonly TESTING_TAB_NAME = TESTING_TAB_NAME;
  ngUnsubscribe = new Subject();
  bootstrapped$: Observable<string>;
  selectedConfig$: Observable<number>;

  testEnabled = false;
  sensorFieldsEnabled = false;
  testCaseEnabled = false;
  testingEnabled = false;
  serviceName: string;
  configs$: Observable<ConfigWrapper<ConfigData>[]> = new Observable();
  schema$: Observable<JSONSchema7> = new Observable();
  selectedConfigIndex: number = undefined;
  fields: FormlyFieldConfig[] = [];
  formlyOptions: any = { autoClear: true };

  testOutput: string;

  onClickTestCase$: Subject<MatTabChangeEvent> = new Subject();
  selectedConfigName: string;

  testCaseMap: any = {};
  testCases: TestCase[] = [];

  routeType = 'edit';
  user$: Observable<string>;
  dynamicFieldsMap$: Observable<Map<string, string>>;
  configs: ConfigWrapper<ConfigData>[];
  testCaseMap$: Observable<TestCaseMap>;
  schema: JSONSchema7;
  testSpec: FormlyFieldConfig[] = [];

  constructor(
    private store: Store<fromStore.State>,
    private config: AppConfigService,
    private formlyJsonschema: FormlyJsonschema,
    private editorService: EditorService
  ) {
    this.store
      .select(fromStore.getServiceName)
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(r => {
        this.serviceName = r;
        this.testEnabled = this.config.getUiMetadata(
          r
        ).testing.perConfigTestEnabled;
        this.sensorFieldsEnabled = this.config.getUiMetadata(
          r
        ).enableSensorFields;
        this.testCaseEnabled = this.config.getUiMetadata(
          r
        ).testing.testCaseEnabled;
        this.testingEnabled = this.config.getUiMetadata(
          r
        ).testing.perConfigTestEnabled;
      });
    this.bootstrapped$ = this.store.select(fromStore.getBootstrapped);
    this.selectedConfig$ = this.store.select(fromStore.getSelectedConfig);
    this.configs$ = this.store.select(fromStore.getConfigs);
    this.schema$ = this.store.select(fromStore.getSchema);
    this.user$ = this.store.select(fromStore.getCurrentUser);
    this.dynamicFieldsMap$ = this.store.select(fromStore.getDynamicFieldsMap);
    this.store
      .select(fromStore.getTestCaseMap)
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(t => (this.testCaseMap = t));
    this.testCaseMap$ = this.store.select(fromStore.getTestCaseMap);
  }

  ngOnInit() {
    this.schema$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(s => {
      this.schema = s;
      this.fields = [
        this.formlyJsonschema.toFieldConfig(cloneDeep(s), this.formlyOptions)
      ];
      this.store.dispatch(
        new fromStore.UpdateDynamicFieldsMap(
          this.formlyJsonschema.dynamicFieldsMap
        )
      );
    });

    this.configs$
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(c => (this.configs = c));

    this.selectedConfig$
      .pipe(
        filter(f => f !== undefined),
        takeUntil(this.ngUnsubscribe)
      )
      .subscribe(s => {
        // due to how oneOf changes the displayed fields we need to provide a freshly generated form when a rule is selected
        if (this.schema) {
          this.fields = [
            this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema))
          ];
        }
        this.selectedConfigName = this.configs[s].name;
        this.testCases =
          cloneDeep(this.testCaseMap[this.selectedConfigName]) || [];
        this.selectedConfigIndex = s;
      });

    this.testCaseMap$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(t => {
      this.testCases =
        cloneDeep(this.testCaseMap[this.selectedConfigName]) || [];
    });

    if (this.config.getUiMetadata(this.serviceName).testing.perConfigTestEnabled) {
      this.editorService.configLoader
        .getTestSpecificationSchema()
        .pipe(takeUntil(this.ngUnsubscribe))
        .subscribe(l => {
            var options = {
              formState: {
                mainModel: {},
                rawObjects: {}
            }};
          this.testSpec = [new FormlyJsonschema().toFieldConfig(l)];
        });
    }
  }

  changeRoute(route) {
    this.store.dispatch(
      new fromStore.Go({
        path: route
      })
    );
  }
}
