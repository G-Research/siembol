import { ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import { AppConfigService } from '@app/services/app-config.service';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { BuildInfoDialogComponent } from '../build-info-dialog/build-info-dialog.component';
import { EditorService } from '../../services/editor.service';
import { AppService } from '../../services/app.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UserRole, RepositoryLinks, repoNames } from '@app/model/config-model';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-nav-bar',
    styleUrls: ['./nav-bar.component.scss'],
    templateUrl: './nav-bar.component.html',
})
export class NavBarComponent implements OnInit {
    user: string;
    userRoles: string[];
    serviceName$: Observable<string>;
    serviceName: string;
    serviceNames: string[];
    environment: string;
    isAdminChecked: boolean;
    isHome: boolean;
    isManagement: boolean;
    repositoryLinks: RepositoryLinks;
    readonly repoNames = repoNames;

    constructor(private config: AppConfigService, 
        private appService: AppService, 
        private editorService: EditorService, 
        private dialog: MatDialog,
        private activeRoute: ActivatedRoute,
        private router: Router) {
        this.user = this.appService.user;
        this.serviceName$ = this.editorService.serviceName$;
        this.serviceNames = this.appService.serviceNames;
        this.environment = this.config.environment;
        this.isAdminChecked = this.editorService.adminMode;
    }

    ngOnInit() {
        this.serviceName$.subscribe(service => {
            if (service) {
                this.userRoles = this.appService.getUserServiceRoles(service);
                this.repositoryLinks = this.appService.getServiceRepositoryLink(service);
            }
            this.serviceName = service;
        });

        this.router.events.subscribe(event => {
            if(event instanceof NavigationEnd) {
                const url = event.urlAfterRedirects;
                this.isHome = this.config.isHomePath('/' + url);
                this.isManagement = this.config.isManagementPath('/' + url)
            }
        });
    }

    showAboutApp() {
        this.dialog.open(BuildInfoDialogComponent, { data: this.config.buildInfo }).afterClosed().subscribe();
    }

    onToggleAdmin() {
        const path = this.isAdminChecked ? this.config.adminPath : "";
        this.router.navigate([this.serviceName + path]);
    }

    getPath(service: string): string {
        let path = service;
        const roles = this.appService.getUserServiceRoles(service);
        const hasMultipleUserRoles = roles.length > 1;
        if ((hasMultipleUserRoles && this.isAdminChecked) || (!hasMultipleUserRoles && roles.includes(UserRole.SERVICE_ADMIN))) {
            path += this.config.adminPath;
        }
        return path;  
    }
}
