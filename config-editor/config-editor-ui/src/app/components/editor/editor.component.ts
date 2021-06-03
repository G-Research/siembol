import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, Config, NAME_REGEX } from '@app/model';
import { Type } from '@app/model/config-model';
import { PopupService } from '@app/services/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { ConfigHistory } from '@app/model/store-state';
import { TabsetTypeComponent } from '@app/ngx-formly/tabset.type.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-generic-editor',
  styleUrls: ['./editor.component.scss'],
  templateUrl: './editor.component.html',
})
export class EditorComponent implements OnInit, OnDestroy {
  @ViewChild(TabsetTypeComponent) tabsetComponent: TabsetTypeComponent;
  titleFormControl = new FormControl('', [Validators.pattern(NAME_REGEX)]);

  public ngUnsubscribe = new Subject();
  public configName: string;
  public configData: ConfigData = {};
  public options: FormlyFormOptions = {};
  public form: FormGroup = new FormGroup({});
  public editedConfig$: Observable<Config>;
  public config: Config;
  private history: ConfigHistory = { past: [], future: [] };
  private inUndoRedo = false;

  @Input() fields: FormlyFieldConfig[];

  constructor(
    public dialog: MatDialog,
    public snackbar: PopupService,
    private editorService: EditorService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {
    this.editedConfig$ = editorService.configStore.editedConfig$;
  }

  ngOnInit() {
    this.editedConfig$.pipe(take(1)).subscribe(config => {
      this.config = config;
      //NOTE: in the form we are using wrapping config to handle optionals, unions
      if (config !== null) {
        this.configData = this.editorService.configSchema.wrapConfig(config.configData);
        this.configName = config.name;
        this.options.formState = {
          mainModel: cloneDeep(this.configData),
        };
      }
    });
  }

  ngAfterViewInit() {
    this.form.valueChanges.subscribe(values => {
      if (
        this.form.valid &&
        !this.inUndoRedo &&
        (this.history.past.length == 0 || JSON.stringify(this.history.past[0].formState) !== JSON.stringify(values))
      ) {
        this.history.past.splice(0, 0, {
          formState: cloneDeep(values),
          tabIndex: this.fields[0].templateOptions.tabIndex,
        });
        this.history.future = [];
        this.updateConfigInStore(values);
      }
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  updateConfigInStoreFromForm() {
    this.updateConfigInStore(this.form.value);
  }

  private updateConfigInStore(configData: ConfigData) {
    const configToClean = cloneDeep(this.config) as Config;
    configToClean.configData = cloneDeep(configData);
    this.config = this.editorService.configSchema.cleanConfig(configToClean);
    this.editorService.configStore.updateEditedConfig(this.config);
  }

  updateConfigData(configData: ConfigData) {
    this.configData = configData;
    this.cd.markForCheck();
  }

  undoConfigInStore() {
    this.inUndoRedo = true;
    this.history.future.splice(0, 0, {
      formState: cloneDeep(this.configData),
      tabIndex: this.fields[0].templateOptions.tabIndex,
    });
    this.history.past.shift();
    this.fields[0].templateOptions.tabIndex = this.history.past[0].tabIndex;
    this.updateConfigInStore(this.history.past[0].formState);
    // this.configData = this.editorService.configSchema.wrapConfig(this.config.configData);
    this.form.patchValue(this.editorService.configSchema.wrapConfig(this.config.configData), { emitEvent: false });

    this.cd.markForCheck();
  }

  redoConfig() {
    this.inUndoRedo = true;
    this.fields[0].templateOptions.tabIndex = this.history.future[0].tabIndex;
    this.updateConfigInStore(this.history.future[0].formState);
    this.configData = this.editorService.configSchema.wrapConfig(this.config.configData);
    this.history.past.splice(0, 0, {
      formState: cloneDeep(this.history.future[0].formState),
      tabIndex: this.history.future[0].tabIndex,
    });
    this.history.future.shift();

    this.cd.markForCheck();
  }

  onSubmit() {
    this.updateConfigInStoreFromForm();
    const dialogRef = this.dialog.open(SubmitDialogComponent, {
      data: {
        name: this.configName,
        type: Type.CONFIG_TYPE,
        validate: () => this.editorService.configStore.validateEditedConfig(),
        submit: () => this.editorService.configStore.submitEditedConfig(),
      },
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(success => {
      if (success) {
        this.router.navigate([this.editorService.serviceName, 'edit'], {
          queryParams: { configName: this.configName },
        });
      }
    });
  }
}
