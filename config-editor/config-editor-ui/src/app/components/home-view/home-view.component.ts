import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { UrlHistoryService } from '@app/services/url-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { Router } from '@angular/router';
import { HelpLink } from '@app/model/app-config';
import { HistoryUrl, ServiceInfo } from '@app/model/config-model';
import { AppService } from '@app/services/app.service';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-home-view',
    styleUrls: ['./home-view.component.scss'],
    templateUrl: './home-view.component.html',  
})
export class HomeViewComponent implements OnInit {
    history: HistoryUrl[];
    root: string;
    homeHelpLinks: HelpLink[];
    userServices: ServiceInfo[];
    
    constructor(private historyService: UrlHistoryService,
                private appConfigService: AppConfigService,
                private appService: AppService,
                private router: Router) { }
    
    ngOnInit() {
        this.userServices = this.appService.userServices;
        this.root = this.appConfigService.serviceRoot.slice(0, -1);
        this.homeHelpLinks = this.appConfigService.homeHelpLinks;
        this.history = this.historyService.getPreviousUrls();
    }

    routeTo(url: string) {
        this.router.navigateByUrl(url);
    }

    openLink(link: string) {
        window.open(link, "_blank");
    }
}