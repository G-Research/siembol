import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { HomeHelpLink } from "@app/model/app-config";
import { AppConfigService } from "@app/services/app-config.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-management-view',
    styleUrls: ['./management-view.component.scss'],
    templateUrl: './management-view.component.html',
})
export class ManagementViewComponent  implements OnInit {
  managementLinks: HomeHelpLink[]; //rename

  constructor(private appConfigService: AppConfigService) {}

  ngOnInit() {
    this.managementLinks = this.appConfigService.homeHelpLinks;
  }

  openLink(link: string) {
    window.open(link, "_blank");
  }
}