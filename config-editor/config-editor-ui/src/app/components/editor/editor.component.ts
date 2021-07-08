import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, Config, NAME_REGEX } from '@app/model';
import { Type } from '@app/model/config-model';
import { PopupService } from '@app/services/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';
import { areJsonEqual } from '@app/commons/helper-functions';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-generic-editor',
  styleUrls: ['./editor.component.scss'],
  templateUrl: './editor.component.html',
})
export class EditorComponent implements OnInit, OnDestroy {
  @Input() field: FormlyFieldConfig;
  titleFormControl = new FormControl('', [Validators.pattern(NAME_REGEX), Validators.required]);
  ngUnsubscribe = new Subject();
  options: FormlyFormOptions = {};
  form: FormGroup = new FormGroup({});
  config: Config;
  configData$: Observable<ConfigData>;
  editedConfig$: Observable<Config>;
  configName: string;

  constructor(
    public dialog: MatDialog,
    public snackbar: PopupService,
    private editorService: EditorService,
    private router: Router
  ) {
    this.editedConfig$ = editorService.configStore.editedConfig$;
    this.configData$ = this.editedConfig$
    .pipe(takeUntil(this.ngUnsubscribe))
    .do(x => {
      this.config = x;
      this.configName = this.config.name;
    })
    .filter(x => 
      !areJsonEqual(x, this.cleanConfig(this.form.value))
    )
    .map(x => this.editorService.configSchema.wrapConfig(x.configData));
  }

  ngOnInit() {
    this.titleFormControl.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(() => {
      if (this.titleFormControl.valid) {
        this.updateConfigInStore();
      }
    });
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(() => {
      if (this.form.valid) {
        this.updateConfigInStore();
      }
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onSubmit() {
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

  private prepareConfig(configData: ConfigData): Config {
    const config = cloneDeep(this.config) as Config;
    config.configData = cloneDeep(configData);
    config.name = this.configName;
    return config;
  }

  private cleanConfig(configData: ConfigData): Config {
    return this.editorService.configSchema.cleanConfig(this.prepareConfig(configData));
  }

  private updateConfigInStore() {
    this.editorService.configStore.updateEditedConfigAndHistory(this.cleanConfig(this.form.value));
  }
}
