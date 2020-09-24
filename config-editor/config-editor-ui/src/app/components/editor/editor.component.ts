import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { EditorService } from '@services/editor.service';
import { ConfigData, ConfigWrapper } from '@app/model';
import { TEST_CASE_TAB_NAME } from '@model/test-case';
import { PopupService } from '@app/popup.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { SubmitDialogComponent } from '..';
import { TESTING_TAB_NAME } from '../../model/test-case';

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
    private config: ConfigWrapper<ConfigData>;

    @Input() fields: FormlyFieldConfig[];
    @Input() onClickTestCase$: Observable<MatTabChangeEvent>;

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

        this.onClickTestCase$.pipe(
            takeUntil(this.ngUnsubscribe),
            filter(f => f.tab.textLabel === TEST_CASE_TAB_NAME || f.tab.textLabel === TESTING_TAB_NAME)
        ).subscribe(() => {
            this.updateConfigInStore();
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    private updateConfigInStore() {
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
