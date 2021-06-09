import { ChangeDetectionStrategy, OnDestroy, ViewChild } from '@angular/core';
import { Component } from '@angular/core';
import { Config } from '@app/model';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { JSONSchema7 } from 'json-schema';
import { cloneDeep } from 'lodash';
import { Observable, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { AdminComponent } from '../admin/admin.component';
import { AdminConfig } from '@app/model/config-model';
import { SchemaService } from '@app/services/schema/schema.service';
import { Router } from '@angular/router';
import { ClipboardService } from '@app/services/clipboard.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-admin-view',
  styleUrls: ['./admin-view.component.scss'],
  templateUrl: './admin-view.component.html',
})
export class AdminViewComponent implements OnDestroy {
  @ViewChild(AdminComponent) adminComponent: AdminComponent;

  ngUnsubscribe = new Subject();
  configData: any;
  serviceName: string;
  schema: JSONSchema7;

  field: FormlyFieldConfig;

  adminConfig$: Observable<AdminConfig>;

  constructor(
    private formlyJsonschema: FormlyJsonschema,
    private editorService: EditorService,
    private router: Router,
    private clipboardService: ClipboardService
  ) {
    this.serviceName = editorService.serviceName;
    this.schema = editorService.adminSchema.schema;
    this.adminConfig$ = editorService.configStore.adminConfig$;
    this.field = this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), { map: SchemaService.renameDescription });
  }

  public ngAfterViewInit() {
    this.adminConfig$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((config: Config) => {
      this.configData = config.configData;
    });
    this.adminConfig$.pipe(take(1)).subscribe((config: Config) => {
      this.field = this.formlyJsonschema.toFieldConfig(cloneDeep(this.schema), {
        map: SchemaService.renameDescription,
      });
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  changeRoute() {
    this.router.navigate([this.serviceName]);
  }

  onClickPaste() {
    this.clipboardService.validateAdminConfig().subscribe(() => {
      let configData = this.editorService.configStore.setEditedPastedAdminConfig();
      this.adminComponent.updateAndWrapConfigData(configData);
      this.adminComponent.addToUndoRedo(configData);
    });
  }

  onClickCopy() {
    this.clipboardService.copy(this.configData);
  }

  onUndo() {
    this.adminComponent.undoConfigInStore();
  }

  onRedo() {
    this.adminComponent.redoConfig();
  }
}
