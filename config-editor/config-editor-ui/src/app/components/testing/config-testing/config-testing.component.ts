import { ChangeDetectionStrategy, Component, Input, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { EditorService } from '@app/services/editor.service';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyForm } from '@ngx-formly/core';
import { ConfigTestResult } from '../../../model/config-model';
import { take } from 'rxjs/operators';
import { FormlyJsonschema } from '@app/ngx-formly/formly-json-schema.service';

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
      this.fields = [new FormlyJsonschema().toFieldConfig(this.editorService.testSpecificationSchema)];
    }
  }

  updateTestSpecification(event: any) {
    this.testSpecification = event;
  }

  runTest() {
    const cleanedTestSpecification = this.editorService.configWrapper
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
