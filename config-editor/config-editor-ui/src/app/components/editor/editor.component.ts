import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
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
import { debounceTime, takeUntil } from 'rxjs/operators';
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
  @Input() field: FormlyFieldConfig;
  @Output() configDataChange: EventEmitter<string> = new EventEmitter<string>();
  titleFormControl = new FormControl('', [Validators.pattern(NAME_REGEX)]);

  public ngUnsubscribe = new Subject();
  public configName: string;
  public configData: ConfigData = {};
  public options: FormlyFormOptions = {};
  public form: FormGroup = new FormGroup({});
  public editedConfig$: Observable<Config>;
  public config: Config;

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
    this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(config => {
      this.config = config;
      if (config) {
        this.configData = this.editorService.configSchema.wrapConfig(config.configData);
        this.configName = config.name;
      }
      this.cd.markForCheck();
    });
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.ngUnsubscribe)).subscribe(values => {
      if (this.form.valid) {
        this.editorService.configStore.addToConfigHistory(this.cleanConfig(cloneDeep(values)));
        this.configDataChange.emit(this.configData);
      }
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  updateConfigInStore() {
    this.editorService.configStore.updateEditedConfig(this.cleanConfig(this.form.value));
  }

  onSubmit() {
    this.updateConfigInStore();
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

  private cleanConfig(configData: ConfigData): Config {
    const configToClean = cloneDeep(this.config) as Config;
    configToClean.configData = cloneDeep(configData);
    configToClean.name = this.configName;
    return this.editorService.configSchema.cleanConfig(configToClean);
  }
}
