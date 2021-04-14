import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { UrlHistoryService } from '@app/services/url-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { Router } from '@angular/router';
import { HomeHelpLink } from '@app/model/app-config';
import { ServiceInfo } from '@app/model/config-model';
import { AppService } from '@app/services/app.service';
import { parseUrl } from '@app/commons/helper-functions'

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-home-view',
    styleUrls: ['./home-view.component.scss'],
    templateUrl: './home-view.component.html',  
})
export class HomeViewComponent implements OnInit {
    history: string[];
    root: string;
    homeHelpLinks: HomeHelpLink[];
    userServices: ServiceInfo[];
    
    constructor(private historyService: UrlHistoryService,
                private appConfigService: AppConfigService,
                private appService: AppService,
                private router: Router) { }
    
    get parseUrl() {
        return parseUrl;
   }

    ngOnInit() {
        this.userServices = this.appService.userServices;
        this.root = this.appConfigService.serviceRoot.slice(0, -1);
        this.homeHelpLinks = this.appConfigService.homeHelpLinks;
        this.history = this.historyService.getHistoryPreviousUrls();
    }

    routeTo(url: string) {
        this.router.navigateByUrl(url);
    }

    openLink(link: string) {
        window.open(link, "_blank");
    }

    
}