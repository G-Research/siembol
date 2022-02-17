import { ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import { AppConfigService } from '@app/services/app-config.service';
import { Observable, Subject } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { BuildInfoDialogComponent } from '../build-info-dialog/build-info-dialog.component';
import { EditorService } from '../../services/editor.service';
import { AppService } from '../../services/app.service';
import { Router, NavigationEnd } from '@angular/router';
import { UserRole, RepositoryLinks, repoNames } from '@app/model/config-model';
import { startWith, takeUntil } from 'rxjs/operators';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-nav-bar',
    styleUrls: ['./nav-bar.component.scss'],
    templateUrl: './nav-bar.component.html',
})
export class NavBarComponent implements OnInit, OnDestroy {
    ngUnsubscribe = new Subject<void>();
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
    isAdminOfAnyService: boolean;
    readonly repoNames = repoNames;

    constructor(private config: AppConfigService, 
        private appService: AppService, 
        private editorService: EditorService, 
        private dialog: MatDialog,
        private router: Router) {
        this.user = this.appService.user;
        this.serviceName$ = this.editorService.serviceName$;
        this.serviceNames = this.appService.serviceNames;
        this.environment = this.config.environment;
        this.isAdminChecked = this.editorService.adminMode;
    }

    ngOnInit() {
        this.isAdminOfAnyService = this.appService.isAdminOfAnyService;
        this.serviceName$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(service => {
            if (service) {
                this.userRoles = this.appService.getUserServiceRoles(service);
                this.repositoryLinks = this.appService.getServiceRepositoryLink(service);
            }
            this.serviceName = service;
        });

        this.router.events
        .pipe(
            startWith(this.router),
            takeUntil(this.ngUnsubscribe))
        .subscribe(event => {
            if (event instanceof NavigationEnd || event instanceof Router){
                this.isHome = this.config.isHomePath(event.url);
                this.isManagement = this.config.isManagementPath(event.url);
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

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
