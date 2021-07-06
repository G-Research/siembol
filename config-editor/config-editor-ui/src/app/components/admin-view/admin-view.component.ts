import { AfterViewInit, ChangeDetectionStrategy, Component, OnDestroy, ViewChild } from '@angular/core';
import { Config } from '@app/model';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { JSONSchema7 } from 'json-schema';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AdminComponent } from '../admin/admin.component';
import { AdminConfig } from '@app/model/config-model';
import { SchemaService } from '@app/services/schema/schema.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-admin-view',
  styleUrls: ['./admin-view.component.scss'],
  templateUrl: './admin-view.component.html',
})
export class AdminViewComponent implements AfterViewInit, OnDestroy {
  @ViewChild(AdminComponent) adminComponent: AdminComponent;

  ngUnsubscribe = new Subject();
  configData: any;

  schema: JSONSchema7;
  field: FormlyFieldConfig;
  adminConfig$: Observable<AdminConfig>;

  constructor(
    private formlyJsonschema: FormlyJsonschema, 
    private editorService: EditorService
  ) {
    this.schema = editorService.adminSchema.schema;
    this.adminConfig$ = editorService.configStore.adminConfig$;
    this.field = this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), {
      map: SchemaService.renameDescription,
    });
  }

  ngAfterViewInit() {
    this.adminConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((config: Config) => {
      this.configData = config.configData || {};
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onClickPaste() {
    this.editorService.configStore.setEditedPastedAdminConfig();
  }

  onClickCopy() {
    this.editorService.configStore.clipboardService.copyFromClipboard(this.configData);
  }

  onClickUndoConfig() {
    this.editorService.configStore.undoAdminConfig();
    this.adminComponent.setMarkHistoryChange();
  }

  onRedoConfig() {
    this.editorService.configStore.redoAdminConfig();
    this.adminComponent.setMarkHistoryChange();
  }
}
