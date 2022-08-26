import { Component, Inject } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { ExistingConfigError, NAME_REGEX, ServiceInfo } from "@app/model/config-model";
import { AppService } from "@app/services/app.service";
import { EditorService } from "@app/services/editor.service";
import { FormlyFieldConfig } from "@ngx-formly/core";


@Component({
    selector: 're-clone-dialog',
    styleUrls: ['clone-dialog.component.scss'],
    templateUrl: 'clone-dialog.component.html',
})
export class CloneDialogComponent {
    fields: FormlyFieldConfig[] = [ 
      {
        key: "config_name",
        type: "input",
        templateOptions: {
          label: "Cloned config name",
          pattern: NAME_REGEX,
          hintEnd: "The name of the cloned config",
          required: true,
        },
      },
      {
        key: "service_instance",
        type: "enum",
        templateOptions: {
          label: "Service Instance",
          multiple: false,
          hintEnd: "The name of the service to clone the config to",
          options: [],
        },
      },
      {
        key: "clone_test_cases",
        type: "checkbox",
        defaultValue: true,
        templateOptions: {
          label: "Include test cases",
        },
      },
    ];
    form: FormGroup = new FormGroup({});
    model = {};
    serviceOptions: string[] = [];
    currentService: string;
    submitting = false;
    constructor(
      private dialogRef: MatDialogRef<CloneDialogComponent>,
      @Inject(MAT_DIALOG_DATA) public data: string,
      private appService: AppService,
      private router: Router,
      private editorService: EditorService
    ) {
      this.currentService = this.editorService.serviceName;
      const userServices = this.appService.userServicesMap;
      const type = userServices.get(this.currentService).type;
      userServices.forEach((value: ServiceInfo, key: string) => {
        if (value.type === type) {
          this.serviceOptions.push(key);
        }
      })
      this.mapFields();
    }

    onClickClose() {
      this.dialogRef.close();
    }

    onClickClone() {
      this.submitting = true;
      this.router.navigate([this.model['service_instance'], 'edit'], {
        queryParams: { 
          newConfigName: this.model['config_name'], 
          cloneConfig: this.data,
          fromService: this.editorService.serviceName,
          withTestCases: this.model['clone_test_cases'] },
      }).then(
        () => {
          this.dialogRef.close();
      }).catch(
        error=> {  
          if (error instanceof ExistingConfigError) {
            this.router.navigate([this.currentService])
          } else {
          this.router.navigate([this.model['service_instance']])
          }
          this.dialogRef.close();
          throw error;
      });
    }

    private mapFields() {
      // Note: serviceOptions have to be computed, so cannot be added into field as constant
      return this.fields.map(f => {
        if (f.key === 'service_instance') {
          f.defaultValue = this.currentService;
          f.templateOptions.options = this.serviceOptions.map(
            function(service) {
              return { value: service, label: service}
            }
          )
        }
      })
    }
}