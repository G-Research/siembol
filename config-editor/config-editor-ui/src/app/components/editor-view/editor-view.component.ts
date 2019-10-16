import { AppConfigService } from '../../config/app-config.service';

import { ChangeDetectionStrategy } from '@angular/core';
import { Component } from '@angular/core';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-editor-view',
    styleUrls: ['./editor-view.component.scss'],
    templateUrl: './editor-view.component.html',
})
export class EditorViewComponent {
    bootstrapped$: Observable<string>;

    testEnabled = false;
    sensorFieldsEnabled = false;
    serviceName: string;

    constructor(private store: Store<fromStore.State>, private config: AppConfigService) {
        this.store.select(fromStore.getServiceName).pipe(take(1)).subscribe(r => {
            this.serviceName = r
            this.testEnabled = this.config.getUiMetadata(r).testing.perConfigTestEnabled;
            this.sensorFieldsEnabled = this.config.getUiMetadata(r).enableSensorFields;
        });
        this.bootstrapped$ = this.store.select(fromStore.getBootstrapped);
    }
}
