import { Component } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { AppConfigService } from '@app/config/app-config.service';
import { Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { BuildInfoDialogComponent } from '../build-info-dialog/build-info-dialog.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-nav-bar',
    styleUrls: ['./nav-bar.component.scss'],
    templateUrl: './nav-bar.component.html',
})
export class NavBarComponent {
    user$: Observable<String>;
    loading$: Observable<boolean>;
    serviceName$: Observable<string>;
    serviceNames$: Observable<string[]>;
    environment: string;

    constructor(private store: Store<fromStore.State>, private config: AppConfigService, private dialog: MatDialog) {
        this.user$ = this.store.select(fromStore.getCurrentUser);
        this.loading$ = this.store.select(fromStore.getLoading);
        this.serviceName$ = this.store.select(fromStore.getServiceName);
        this.serviceNames$ = this.store.select(fromStore.getServiceNames);
        this.environment = this.config.environment;
    }

    public onSelectView(view: string) {
        this.store.dispatch(new fromStore.Go({
            extras: {
                queryParamsHandling: 'merge',
            },
            path: ['id', view],
            query: {},
        }));
    }

    public showAboutApp() {
        this.dialog.open(BuildInfoDialogComponent, {data: this.config.getBuildInfo}).afterClosed().subscribe();
    }
}
