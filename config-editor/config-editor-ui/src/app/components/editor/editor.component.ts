import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, Config } from '@app/model';
import { Type } from '@app/model/config-model';
import { PopupService } from '@app/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SubmitDialogComponent } from '../submit-dialog/submit-dialog.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-generic-editor',
    styleUrls: ['./editor.component.scss'],
    templateUrl: './editor.component.html',
})
export class EditorComponent implements OnInit, OnDestroy {
    public ngUnsubscribe = new Subject();
    public configName: string;
    public configData: ConfigData = {};
    public options: FormlyFormOptions = {};
    public form: FormGroup = new FormGroup({});
    public editedConfig$: Observable<Config>;
    public config: Config;

    @Input() fields: FormlyFieldConfig[];

    constructor(public dialog: MatDialog, public snackbar: PopupService,
        private editorService: EditorService, private router: Router) {
        this.editedConfig$ = editorService.configStore.editedConfig$;
    }

    ngOnInit() {
        this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(config => {
            this.config = config;
            //NOTE: in the form we are using wrapping config to handle optionals, unions
            this.configData = this.editorService.configSchema.wrapConfig(config.configData);
            this.configName = this.config.name;
            this.options.formState = {
                mainModel: this.configData,
            }
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    updateConfigInStore() {
        const configToClean = cloneDeep(this.config) as Config;
        configToClean.configData = cloneDeep(this.form.value);
        configToClean.name = this.configName;
        const configToUpdate = this.editorService.configSchema.cleanConfig(configToClean);
        this.editorService.configStore.updateEditedConfig(configToUpdate);
    }

    onSubmit() {
        this.updateConfigInStore();
        const dialogRef = this.dialog.open(SubmitDialogComponent,
            {
                data: {
                    name: this.configName,
                    type: Type.CONFIG_TYPE,
                    validate: () => this.editorService.configStore.validateEditedConfig(),
                    submit: () => this.editorService.configStore.submitEditedConfig()
                },
                disableClose: true
            });

        dialogRef.afterClosed().subscribe(
            success => {
                if (success) {
                    this.router.navigate(
                        [this.editorService.serviceName, 'edit'],
                        { queryParams: { configName: this.configName } }
                    );
                }
            }
        );
    }
}
