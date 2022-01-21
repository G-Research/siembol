import { ChangeDetectionStrategy, Component, OnInit, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { HelpLink } from "@app/model/app-config";
import { AppConfigService } from "@app/services/app-config.service";
import { AppService } from "@app/services/app.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-management-view',
    styleUrls: ['./management-view.component.scss'],
    templateUrl: './management-view.component.html',
})
export class ManagementViewComponent  implements OnInit {
  managementLinks: HelpLink[];
  dialogref: MatDialogRef<any>;

  constructor(
    private appConfigService: AppConfigService,
    private appService: AppService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.managementLinks = this.appConfigService.managementLinks;
  }

  openLink(link: string) {
    window.open(link, "_blank");
  }

  openConfirmRestartAllApplications(templateRef: TemplateRef<any>) { 
    this.appService.getAllApplications().subscribe(applications => {
      this.dialogref = this.dialog.open(
        templateRef, 
        { 
          data: applications.map(app => app.topology_name),
          maxWidth: '800px',
        });
    })
  }

  restartAllApplications(templateRef: TemplateRef<any>) {
    this.appService.restartAllApplications().subscribe(applications => {
      this.dialogref = this.dialog.open(
        templateRef, 
        { 
          data: applications.map(app => app.topology_name),
          maxWidth: '800px',
        });
    })
  }

  onClickClose() {
    this.dialogref.close();
  }
}