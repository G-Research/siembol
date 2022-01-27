import { Component, Inject } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { ServiceInfo } from "@app/model/config-model";
import { AppService } from "@app/services/app.service";
import { EditorService } from "@app/services/editor.service";
import { FormlyFieldConfig } from "@ngx-formly/core";


@Component({
    selector: 're-clone-dialog',
    styleUrls: ['clone-dialog.component.scss'],
    templateUrl: 'clone-dialog.component.html',
})
export class CloneDialogComponent {
    fields: FormlyFieldConfig[] =  
    [ 
      {
        key: 'config_name',
        type: 'input',
        templateOptions: {
          label: 'Cloned config name',
          pattern: "^[a-zA-Z0-9_\\-]+$",
          hintEnd: "The name of the cloned config",
          required: true,
        },
      },
      {
        key: 'clone_test_cases',
        type: 'checkbox',
        defaultValue: true,
        templateOptions: {
          label: 'Include test cases',
        },
      },
      {
        key: 'service_instance',
        type: 'enum',
        templateOptions: {
          label: "Service Instance",
          multiple: false,
          options: [],
        },
      },
    ]

    form: FormGroup = new FormGroup({});
    model = {};
    serviceOptions: string[] = [];
    currentService: string;
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
      this.router.navigate([this.editorService.serviceName, 'edit'], {
        queryParams: { 
          cloneConfig: this.data, 
          withTests: this.model['clone_test_cases'], 
          newName: this.model['config_name'], 
          },
      });

      // if with test cases:
      // if new service -> create new serviceContext
      // have to submit both config+tests to the store
      // redirect to new config

      // bulk cloning? 
      // option to not redirect?
    }

    private mapFields() {
      return this.fields.map(f => {
        if (f.key === 'service_instance') {
          f.defaultValue = this.currentService;
          f.templateOptions.options = this.serviceOptions.map(
            function(service) {
              return { "value": service,"label": service}
            }
          )
        }
      })
    }
}