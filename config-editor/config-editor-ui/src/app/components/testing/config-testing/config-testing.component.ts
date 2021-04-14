import { ChangeDetectionStrategy, Component, OnInit, ViewChild, ChangeDetectorRef, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyForm } from '@ngx-formly/core';
import { ConfigTestResult, TestingType } from '../../../model/config-model';
import { take } from 'rxjs/operators';
import { FormlyJsonschema } from '@ngx-formly/core/json-schema';
import { SchemaService } from '@app/services/schema/schema.service';

@Component({
  selector: 're-config-testing',
  templateUrl: './config-testing.component.html',
  styleUrls: ['./config-testing.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigTestingComponent implements OnInit {
  testInput: any = {}

  fields: FormlyFieldConfig[] = [];
  options: FormlyFormOptions = {
    formState: {
      mainModel: {},
      rawObjects: {}
    }
  };
  @Input() testingType: TestingType;
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
      this.fields = [new FormlyJsonschema().toFieldConfig(schema, {map: SchemaService.renameDescription})];
    }
  }

  runTest() {
    const cleanedTestSpecification = this.editorService.configSchema
      .cleanRawObjects(this.form.value, this.formly.options.formState.rawObjects);
    
    this.editorService.configStore.testService.test(cleanedTestSpecification, this.testingType).pipe(take(1))
      .subscribe((r: ConfigTestResult) => {
        this.output = r;
        this.isInvalid = r !== undefined ? false : true;
        this.cd.markForCheck();
      }
    )
  }
}
