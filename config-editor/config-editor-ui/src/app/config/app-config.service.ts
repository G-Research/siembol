import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { ConfigData } from '../model/config-model';
import { UiMetadataMap } from '../model/ui-metadata-map';
import { EditorConfig } from './editor-config';

@Injectable({
    providedIn: 'root',
  })
export class AppConfigService {

    private config: EditorConfig;
    private uiMetadata: UiMetadataMap;

    constructor(private http: HttpClient) { }

    // This gets called on startup and APP_INITIALIZER will wait for the promise to resolve
    public loadConfig(): Promise<any> {
        return this.http.get('config.json')
            .toPromise()
            .then((r: ConfigData) => {
                // tslint:disable-next-line:no-console
                console.info(`Loaded ${r.environment} config`, r);
                this.config = r;
            });
    }

    public loadUiMetadata(): Promise<any> {
        return this.http.get('assets/uiSetupConfig.json')
            .toPromise()
            .then((r: UiMetadataMap) => {
                // tslint:disable-next-line:no-console
                console.info('loaded UI setup', r);
                this.uiMetadata = r;
            })
    }

    public getServiceList(): string[] {
        return Object.keys(this.uiMetadata);
    }

    public getUiMetadata(serviceName: string): UiMetadataMap {
        return this.uiMetadata[serviceName];
    }

    public getConfig(): EditorConfig {
        return this.config;
    }

    public get environment(): string {
        return this.config.environment;
    }

    public get serviceRoot(): string {
        return this.config.serviceRoot;
    }
}
