import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, Routes, Route } from '@angular/router';
import { Subject } from 'rxjs';
import { ConfigManagerComponent, LandingPageComponent } from '..';
import { HomeComponent, PageNotFoundComponent } from '../../containers';

import { TestCaseHelpComponent } from '../testing/test-case-help/test-case-help.component';
import { AppService } from '../../services/app.service';
import { AdminGuard, AuthGuard, ConfigEditGuard, EditorServiceGuard } from '@app/guards';
import { AppConfigService } from '@app/services/app-config.service';
import { EditorViewComponent } from '../editor-view/editor-view.component';
import { takeUntil } from 'rxjs/operators';
import { AdminViewComponent } from '../admin-view/admin-view.component';
import { UserRole } from '@app/model/config-model';
import { cloneDeep } from 'lodash';
import { HomeViewComponent } from '../home-view/home-view.component';
import { ManagementViewComponent } from '../management-view/management-view.component';

@Component({
    template: '',
})
export class AppInitComponent implements OnInit, OnDestroy {
    private ngUnsubscribe = new Subject<void>();

    private readonly configRoutes: Routes = [
        {
            path: '',
            component: ConfigManagerComponent,
            canActivate: [AuthGuard, EditorServiceGuard],
        },
        {
            component: EditorViewComponent,
            path: 'edit',
            canActivate: [AuthGuard, EditorServiceGuard],
            children: [{
                path: '',
                component: EditorViewComponent,
                canActivate: [ConfigEditGuard],
                runGuardsAndResolvers: 'paramsOrQueryParamsChange'
            }],
            runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        }]

    private readonly adminRoute: Route = 
        {
            path: 'admin',
            component: AdminViewComponent,
            canActivate: [AuthGuard, AdminGuard],
        }


    private appRoutes: Routes = [
        {
            path: '',
            pathMatch: 'full',
            redirectTo: 'home',
        },
        {
            component: TestCaseHelpComponent,
            path: 'help/testcase',
        },
    ];

    private homeRoute: Route = {
        path: 'home',
        component: LandingPageComponent,
        children: [
            { path: '', component: HomeViewComponent },
        ],
    }

    constructor(private router: Router,
        private appService: AppService,
        private config: AppConfigService) { }

    ngOnInit() {
        this.appService.loaded$.pipe(takeUntil(this.ngUnsubscribe)).subscribe((loaded: boolean) => {
            if (loaded) {
                this.loadRoutes();
                const url = this.config.authenticationService.isCallbackUrl(this.router.url)
                    ? this.config.authenticationService.redirectedUrl : this.router.url;
                this.router.navigateByUrl(url);
            }
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.complete();
    }

    private loadRoutes() {
        const routes = this.appRoutes;
        this.appService.serviceNames.forEach(s => {
            const userRoles = this.appService.getUserServiceRoles(s);
            let childrenRoutes = [];
            if (userRoles.includes(UserRole.SERVICE_USER)) {
                childrenRoutes = cloneDeep(this.configRoutes);
            }
            if (userRoles.includes(UserRole.SERVICE_ADMIN)) {
                childrenRoutes.push(this.adminRoute);
            }
            this.appRoutes.push({
                path: s, component: HomeComponent, children: childrenRoutes
            })
            
        });
        if (this.appService.isAdminOfAnyService) {
            this.homeRoute.children.push({ path: 'management', component: ManagementViewComponent })
        }
        routes.push(this.homeRoute);
        routes.push({
            component: PageNotFoundComponent,
            path: '**'
        });
        this.router.resetConfig(routes);
    }
}