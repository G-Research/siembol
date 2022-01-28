import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { HelpLink } from "@app/model/app-config";
import { AppConfigService } from "@app/services/app-config.service";
import { AppService } from "@app/services/app.service";
import { ApplicationDialogComponent } from "..";

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
    private dialog: MatDialog,
    private service: AppService
  ) {}

  ngOnInit() {
    this.managementLinks = this.appConfigService.managementLinks;
  }

  openLink(link: string) {
    window.open(link, "_blank");
  }

  openApplicationDialog() {
    this.service.getAllApplications().subscribe(apps => 
      this.dialog.open(ApplicationDialogComponent, { data: apps })
    );
  }
}