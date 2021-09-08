import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Importer } from '@app/model/config-model';
import { EditorService } from '@app/services/editor.service';
import { SchemaService } from '@app/services/schema/schema.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import * as yaml from 'js-yaml';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-importer-dialog',
  styleUrls: ['importer-dialog.component.scss'],
  templateUrl: 'importer-dialog.component.html',
})
export class ImporterDialogComponent {
  readonly placeholder = `paste config here`;
  field: FormlyFieldConfig;
  options: FormlyFormOptions = {};
  form: FormGroup = new FormGroup({});
  model: any = {};
  editorOptions = 
  {
    theme: 'vs-dark', 
    language: 'yaml', 
    scrollBeyondLastLine: false, 
    fontSize: "14px",
  };
  config: string;
  name: string;
  editor: any;

  constructor(
    public dialogref: MatDialogRef<ImporterDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Importer,
    private service: EditorService, 
    private formlyJsonschema: FormlyJsonschema
    ) {
      this.name = data.importer_name; 
      const schema = data.importer_attributes_schema;
      this.service.configSchema.formatTitlesInSchema(schema, '');
      this.field = this.formlyJsonschema.toFieldConfig(schema, {
        map: SchemaService.renameDescription,
      });
    }

    onClickClose() {
      this.dialogref.close();
    }

    onClickImport() {
      this.validateYaml();
      const configToImport = {
        importer_name: this.name,
        importer_attributes: this.service.configSchema.removeEmptyArrays(this.form.value),
        config_to_import: this.config,
      }
      return this.service.configStore.importConfig(configToImport).subscribe(result => {
        this.dialogref.close(result);
      });
    }

    hidePlaceholder() {
      document.getElementById("placeholder").style.display="none";
      this.editor.focus();
    }

    onInit(event: any) {
      this.editor = event;
    }

    private validateYaml() {
      try {
        yaml.load(this.config);
      } catch (e) {
        throw Error("Config is not valid YAML: " + e);
      }
    }
}