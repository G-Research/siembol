import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ErrorDialogComponent } from '@app/components';
import { ConfigData, ConfigWrapper, EditorResult } from '@app/model';
import { ConfigTestDto, ConfigTestResult } from '@app/model/config-model';
import { TestCase, TestState } from '@app/model/test-case';
import { PopupService } from '@app/popup.service';
import { EditorService } from '@app/services/editor.service';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { FormlyForm } from '@ngx-formly/core';
import { cloneDeep } from 'lodash';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
@Component({
  selector: 're-config-testing',
  templateUrl: './config-testing.component.html',
  styleUrls: ['./config-testing.component.scss'],
  changeDetection: ChangeDetectionStrategy.Default
})
export class ConfigTestingComponent implements OnInit {
  @Input() fields: FormlyFieldConfig[] = [];
  @Input() selectedConfig: ConfigWrapper<ConfigData>;
testSpecOutput: any = {};
  @Input() options: FormlyFormOptions = {
    formState: {
      mainModel: {},
      rawObjects: {}
    }
  };
  @Input() testInput: any = {};

  @ViewChild('formly', { static: true }) formly: FormlyForm;
  public form: FormGroup = new FormGroup({});
  public testState = TestState;
  public isInvalid = false;

  public output: any;
  constructor(
    private store: Store<fromStore.State>,
    private editorService: EditorService,
    private dialog: MatDialog,
    private snackbar: PopupService,
    private changeDetector: ChangeDetectorRef
  ) {}

  ngOnInit() {}

  updateOutput(event: TestCase) {
    this.testSpecOutput = event;
  }

  private replacer(key, value) {
    return value === null ? undefined : value;
  }

  runSingleTest(testOutput: any) {
    this.testSingleConfig(testOutput).subscribe(s => {this.changeDetector.markForCheck()}, err => console.error(err));
  }

  private testSingleConfig(test: any): Observable<any> {
    const testDto: ConfigTestDto = {
      files: [
        {
          content: this.selectedConfig.configData
        }
      ],
      test_specification: JSON.parse(
        JSON.stringify(test, this.replacer)
      )
    };

    return this.editorService.configLoader.testSingleConfig(testDto).pipe(
      map((r: EditorResult<ConfigTestResult>) => {
        this.output = r.attributes;
        if (r.status_code === 'OK') {
          this.isInvalid = false;
          return r.attributes.test_result_raw_output;
        }
        else if (r.status_code === 'ERROR') {
            this.isInvalid = true;
            return r;
        }
        this.dialog
          .open(ErrorDialogComponent, {
            data: {
              message: r.attributes.message,
              error: {
                attributes: {
                  message: r.attributes.exception
                }
              }
            }
          })
          .afterClosed()
          .subscribe();

        return null;
      }),
      catchError((err: HttpErrorResponse) => {
        this.snackbar.openSnackBar(err, `Error could not contact backend`);

        return throwError(err);
      })
    );
  }

  formatOutput(): TestCase {
    const out = cloneDeep(this.testSpecOutput);
    for (const element in this.formly.options.formState.rawObjects) {
        out[element] = this.formly.options.formState.rawObjects[element];
    }
    return out;
  }
}
