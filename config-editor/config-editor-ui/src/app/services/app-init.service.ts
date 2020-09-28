import { Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AppConfigService } from '../config';
import { HomeComponent, PageNotFoundComponent } from '../containers';
import { TestCaseHelpComponent } from '../components/testing/test-case-help/test-case-help.component';
import { LandingPageComponent, ConfigManagerComponent } from '../components';
import { EditorServiceGuard, ConfigEditGuard } from '../guards';
import { EditorViewComponent } from '../components/editor-view/editor-view.component';

@Injectable({
    providedIn: 'root',
})
export class AppInitService {
    private readonly specificEditorRoutes: Routes = [
        {
            path: '',
            component: ConfigManagerComponent,
            canActivate: [EditorServiceGuard],
        },
        {
            component: EditorViewComponent,
            path: 'edit',
            canActivate: [EditorServiceGuard],
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

    constructor(private router: Router, private config: AppConfigService) { }

    loadRoutes(): Promise<any> {
        return new Promise<any>((resolve, reject) => {

            const routes = this.appRoutes;
            this.config.getServiceNames().forEach(r => {
                this.appRoutes.push({ path: r, component: HomeComponent, children: this.specificEditorRoutes })
            });
            routes.push({
                component: PageNotFoundComponent,
                path: '**',
            });
            this.router.resetConfig(routes);
            resolve();
        });
    }
}