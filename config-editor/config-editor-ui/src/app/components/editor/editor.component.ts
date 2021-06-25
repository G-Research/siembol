import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, Config, NAME_REGEX } from '@app/model';
import { Type } from '@app/model/config-model';
import { PopupService } from '@app/services/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { debounceTime, take, takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { ConfigHistoryService } from '@app/services/config-history.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-generic-editor',
  styleUrls: ['./editor.component.scss'],
  templateUrl: './editor.component.html',
  providers: [ConfigHistoryService],
})
export class EditorComponent implements OnInit, OnDestroy {
  titleFormControl = new FormControl('', [Validators.pattern(NAME_REGEX)]);

  public ngUnsubscribe = new Subject();
  public configName: string;
  public configData: ConfigData = {};
  public options: FormlyFormOptions = {};
  public form: FormGroup = new FormGroup({});
  public editedConfig$: Observable<Config>;
  public config: Config;

  @Input() field: FormlyFieldConfig;

  constructor(
    public dialog: MatDialog,
    public snackbar: PopupService,
    private editorService: EditorService,
    private router: Router,
    private cd: ChangeDetectorRef,
    private configHistoryService: ConfigHistoryService
  ) {
    this.editedConfig$ = editorService.configStore.editedConfig$;
  }

  ngOnInit() {
    this.editedConfig$.pipe(take(1)).subscribe(config => {
      //NOTE: in the form we are using wrapping config to handle optionals, unions
      if (config) {
        this.configData = this.editorService.configSchema.wrapConfig(config.configData);
        this.configName = config.name;
      }
    });
    this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(config => {
      this.config = config;
      this.cd.markForCheck();
    });
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(values => {
      if (this.form.valid) {
        this.addToConfigHistory(cloneDeep(values), this.field.templateOptions.tabIndex);
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

  updateConfigData(configData: ConfigData) {
    this.configData = cloneDeep(configData);
    this.cd.markForCheck();
  }

  addToConfigHistory(config: any, tabIndex: number = 0) {
    this.configHistoryService.addConfig(config, tabIndex);
  }

  undoConfig() {
    let nextState = this.configHistoryService.undoConfig();
    this.field.templateOptions.tabIndex = nextState.tabIndex;
    this.updateConfigInStore(nextState.formState);
    this.updateConfigData(nextState.formState);
  }

  redoConfig() {
    let nextState = this.configHistoryService.redoConfig();
    this.field.templateOptions.tabIndex = nextState.tabIndex;
    this.updateConfigInStore(nextState.formState);
    this.updateConfigData(nextState.formState);
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
        this.configHistoryService.clear();
        this.addToConfigHistory(cloneDeep(this.configData), this.field.templateOptions.tabIndex);
      }
    });
  }

  private updateConfigInStore(configData: ConfigData) {
    const configToClean = cloneDeep(this.config) as Config;
    configToClean.configData = cloneDeep(configData);
    configToClean.name = this.configName;
    this.editorService.configStore.updateEditedConfig(this.editorService.configSchema.cleanConfig(configToClean));
  }
}
