import { ChangeDetectionStrategy, Component, Input, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyForm } from '@ngx-formly/core';
import { ConfigTestResult } from '../../../model/config-model';
import { take } from 'rxjs/operators';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';

@Component({
  selector: 're-config-testing',
  templateUrl: './config-testing.component.html',
  styleUrls: ['./config-testing.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigTestingComponent implements OnInit {
  testSpecification: any = {};
  testInput: any = {}

  fields: FormlyFieldConfig[] = [];
  formlyOptions: any;
  options: FormlyFormOptions = {
    formState: {
      mainModel: {},
      rawObjects: {}
    }
  };
  @ViewChild('formly', { static: true }) formly: FormlyForm;
  public form: FormGroup = new FormGroup({});
  public isInvalid = false;
  public output: any;

  constructor(
    private editorService: EditorService,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit() {
    if (this.editorService.metaDataMap.testing.perConfigTestEnabled) {
      let schema = this.editorService.testSpecificationSchema;
      this.editorService.configSchema.formatTitlesInSchema(schema, '');
      this.formlyOptions = {
        autoClear: true,
        map: this.editorService.configSchema.mapSchemaForm
      }
      this.fields = [new FormlyJsonschema().toFieldConfig(schema, this.formlyOptions)];
    }
  }

  updateTestSpecification(event: any) {
    this.testSpecification = event;
  }

  runTest() {
    const cleanedTestSpecification = this.editorService.configSchema
      .cleanRawObjects(this.testSpecification, this.formly.options.formState.rawObjects);

    this.editorService.configStore.testService.testEditedConfig(cleanedTestSpecification).pipe(take(1))
      .subscribe((r: ConfigTestResult) => {
        this.output = r;
        this.isInvalid = r !== undefined ? false : true;
        this.cd.markForCheck();
      }
      );
  }
}
