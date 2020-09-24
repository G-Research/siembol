import { Component } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { AppConfigService } from '@app/config/app-config.service';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { BuildInfoDialogComponent } from '../build-info-dialog/build-info-dialog.component';
import { EditorService } from '../../services/editor.service';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-nav-bar',
    styleUrls: ['./nav-bar.component.scss'],
    templateUrl: './nav-bar.component.html',
})
export class NavBarComponent {
    user: String;
    loading$: Observable<boolean>;
    serviceName$: Observable<string>;
    serviceNames: string[];
    environment: string;

    constructor(private config: AppConfigService, private editorService: EditorService, private dialog: MatDialog) {
        this.user = this.config.getUser();
        this.serviceName$ = this.editorService.serviceName$;
        this.serviceNames = this.config.getServiceNames();
        this.environment = this.config.environment;
    }

    public showAboutApp() {
        this.dialog.open(BuildInfoDialogComponent, { data: this.config.getBuildInfo }).afterClosed().subscribe();
    }
}
