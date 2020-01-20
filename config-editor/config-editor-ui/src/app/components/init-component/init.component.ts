import { Component, OnDestroy } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { delay, filter, map, switchMap, takeUntil, withLatestFrom } from 'rxjs/operators';
import { ConfigManagerComponent, EditorViewComponent, LandingPageComponent } from '..';
import { HomeComponent, PageNotFoundComponent } from '../../containers';
import { EditorService } from '../../editor.service';
import * as fromGuards from '../../guards';
import { RepoResolver, ViewResolver } from '../../guards';
import * as fromStore from '../../store';
import { SetServiceNames } from '../../store';
import { TestCaseHelpComponent } from '../testing/test-case-help/test-case-help.component';

@Component({
    template: '',
})
export class InitComponent implements OnDestroy {

    private readonly specificEditorRoutes: Routes = [
        {
            path: '',
            component: ConfigManagerComponent,
            resolve: {
                message: ViewResolver,
            },
        },
        {
            path: 'edit',
            redirectTo: 'edit/0',
        },
        {
            component: EditorViewComponent,
            path: 'edit/:id',
            canActivate: [fromGuards.ConfigStoreGuard],
        },
        {
            path: 'test',
            redirectTo: 'test/0',
        },
        {
            component: EditorViewComponent,
            path: 'test/:id',
            canActivate: [fromGuards.ConfigStoreGuard],
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
            resolve: {
                message: RepoResolver,
            },
        },
    ];

    private ngUnsubscribe = new Subject();
    constructor(private router: Router, private store: Store<fromStore.State>,
            private service: EditorService) {
        this.service.getServiceNames().pipe(takeUntil(this.ngUnsubscribe)).subscribe((serviceList: string[]) => {
            const routes = this.appRoutes;
            serviceList.forEach(r => {
                routes.push({path: r, component: HomeComponent, children: this.specificEditorRoutes})
            });
            routes.push({
              component: PageNotFoundComponent,
              path: '**',
            });
            this.router.resetConfig(routes);
            this.store.dispatch(new SetServiceNames(serviceList));
            this.service.createLoaders();
        })

        this.store.select(fromStore.getServiceNames).pipe(
            takeUntil(this.ngUnsubscribe),
            switchMap(_ => {
                this.router.navigate([this.router.url]);

                return this.store.select(fromStore.getBootstrapped).pipe(
                    withLatestFrom(this.store.select(fromStore.getServiceName)),
                    filter(([bootstrapped, serviceName]) => bootstrapped !== undefined && bootstrapped === serviceName),
                    delay(100),
                    map(() => {
                        this.router.navigate([this.router.url]);
                    })
                )
            })
        ).subscribe();
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
    }
}
