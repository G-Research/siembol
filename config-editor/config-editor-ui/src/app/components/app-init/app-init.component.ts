import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, Routes, Route } from '@angular/router';
import { Subject } from 'rxjs';
import { ConfigManagerComponent, LandingPageComponent } from '..';
import { HomeComponent, PageNotFoundComponent } from '../../containers';

import { TestCaseHelpComponent } from '../testing/test-case-help/test-case-help.component';
import { AppService } from '../../services/app.service';
import { EditorServiceGuard } from '@app/guards';
import { ConfigEditGuard } from '@app/guards';
import { AppConfigService } from '@app/services/app-config.service';
import { EditorViewComponent } from '../editor-view/editor-view.component';
import { takeUntil } from 'rxjs/operators';
import { AuthGuard } from '@app/guards';
import { AdminViewComponent } from '../admin-view/admin-view.component';
import { AdminGuard } from '@app/guards';
import { UserRole } from '@app/model/config-model';
import { cloneDeep } from 'lodash';

@Component({
    template: '',
})
export class AppInitComponent implements OnInit, OnDestroy {
    private ngUnsubscribe = new Subject();

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
            path: 'home',
            component: LandingPageComponent,
        },
        {
            component: TestCaseHelpComponent,
            path: 'help/testcase',
        }
    ];

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

    private loadRoutes() {
        const routes = this.appRoutes;
        this.appService.serviceNames.forEach(s => {
            let userRoles = this.appService.getUserServiceRoles(s);
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
        routes.push({
            component: PageNotFoundComponent,
            path: '**'
        });
        this.router.resetConfig(routes);
    }

    ngOnDestroy() {
        this.ngUnsubscribe.complete();
    }
}