import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { Subject } from 'rxjs';
import { ConfigManagerComponent, LandingPageComponent } from '..';
import { HomeComponent, PageNotFoundComponent } from '../../containers';

import { TestCaseHelpComponent } from '../testing/test-case-help/test-case-help.component';
import { AppService } from '../../services/app.service';
import { EditorServiceGuard } from '../../guards/editor-service.guard';
import { ConfigEditGuard } from '../../guards/config-edit.guard';
import { AppConfigService } from '../../config';
import { EditorViewComponent } from '../editor-view/editor-view.component';
import { takeUntil } from 'rxjs/operators';
import { AuthGuard } from '@app/guards/auth-guard';

@Component({
    template: '',
})
export class AppInitComponent implements OnInit, OnDestroy {
    private ngUnsubscribe = new Subject();

    private readonly specificEditorRoutes: Routes = [
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
        },
    ]

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
        this.appService.serviceNames.forEach(r => {
            this.appRoutes.push({ path: r, component: HomeComponent, children: this.specificEditorRoutes })
        });
        routes.push({
            component: PageNotFoundComponent,
            path: '**',
        });
        this.router.resetConfig(routes);
    }

    ngOnDestroy() {
        this.ngUnsubscribe.complete();
    }
}