import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EditorService } from '@services/editor.service';
import { ConfigData, ConfigWrapper } from '@app/model';
import { PopupService } from '@app/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
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
    public editedConfig$: Observable<ConfigWrapper<ConfigData>>;
    public config: ConfigWrapper<ConfigData>;

    @Input() fields: FormlyFieldConfig[];

    constructor(public dialog: MatDialog, public snackbar: PopupService,
        private editorService: EditorService) {
        this.editedConfig$ = editorService.configStore.editedConfig$;
    }

    ngOnInit() {
        this.editedConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(config => {
            this.config = config;
            this.configData = cloneDeep(this.config.configData);
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
        const configToClean = cloneDeep(this.config) as ConfigWrapper<ConfigData>;
        configToClean.configData = cloneDeep(this.form.value);
        configToClean.name = this.configName;
        const configToUpdate = this.editorService.cleanConfig(configToClean);
        this.editorService.configStore.updateEditedConfig(configToUpdate);
    }

    onSubmit() {
        this.updateConfigInStore();
        this.dialog.open(SubmitDialogComponent, { data: this.configName });
    }
}
