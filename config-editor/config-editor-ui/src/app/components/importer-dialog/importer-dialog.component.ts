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
  
  constructor(
    public dialogref: MatDialogRef<ImporterDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Importer,
    private service: EditorService, 
    private formlyJsonschema: FormlyJsonschema
    ) {
      this.name = data.importer_name; 
      this.config = `<paste ${this.name} config here>`;
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

    private validateYaml() {
      try {
        yaml.load(this.config);
      } catch (e) {
        throw Error("Config is not valid YAML: " + e);
      }
    }
}