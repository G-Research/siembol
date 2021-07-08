import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { ConfigData } from '../model/config-model';
import { UiMetadataMap } from '../model/ui-metadata-map';
import {
  IAuthenticationService,
  DefaultAuthenticationService,
  KerberosAuthenticationService,
} from './authentication.service';
import { Oauth2AuthenticationService } from '@app/services/oauth2-authentication.service';
import { AppConfig, AuthenticationType, BuildInfo } from '../model';
import { HomeHelpLink } from '@app/model/app-config';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private _config: AppConfig;
  private _uiMetadata: UiMetadataMap;
  private _buildInfo: BuildInfo;
  private _authenticationService: IAuthenticationService;

  constructor(private http: HttpClient) {
    this._authenticationService = new DefaultAuthenticationService();
  }

  loadConfigAndMetadata(): Promise<any> {
    return this.loadConfig()
      .then(() => this.loadUiMetadata())
      .then(() => this.createAuthenticationService());
  }

  loadBuildInfo(): Promise<any> {
    return this.http
      .get('assets/build-info.json')
      .toPromise()
      .then((r: BuildInfo) => {
        console.info('loaded app metadata', r);
        this._buildInfo = r;
      })
      .catch(err => console.info(`could not load build info: ${err}`));
  }

  isHomePath(path: string): boolean {
    if (path === '/home' || path === '/') {
      return true;
    }
    return false;
  }

  isNewConfig(path: string): boolean {
    if (
      path.includes('newConfig=true') ||
      path.includes('pasteConfig=true') ||
      path.includes('newTestCase=true') ||
      path.includes('pasteTestCase=true')
    ) {
      return true;
    }
    return false;
  }

  get adminPath(): string {
    return '/admin';
  }

  get config(): AppConfig {
    return this._config;
  }

  get buildInfo(): BuildInfo {
    return this._buildInfo;
  }

  get environment(): string {
    return this._config.environment;
  }

  get serviceRoot(): string {
    return this._config.serviceRoot;
  }

  get uiMetadata(): UiMetadataMap {
    return this._uiMetadata;
  }

  get authenticationService(): IAuthenticationService {
    return this._authenticationService;
  }

  get homeHelpLinks(): HomeHelpLink[] {
    return this._config.homeHelpLinks;
  }

  get historyMaxSize(): number {
    return this._config.historyMaxSize ? this._config.historyMaxSize : 5;
  }

  get blockingTimeout(): number {
    return this._config.blockingTimeout ? this._config.blockingTimeout : 30000;
  }

  private loadConfig(): Promise<any> {
    return this.http
      .get('config/ui-config.json')
      .toPromise()
      .then((r: ConfigData) => {
        console.info(`Loaded ${r.environment} config`, r);
        this._config = r;
      });
  }

  private loadUiMetadata(): Promise<any> {
    return this.http
      .get('config/ui-bootstrap.json')
      .toPromise()
      .then((r: UiMetadataMap) => {
        console.info('loaded UI setup', r);
        this._uiMetadata = r;
      });
  }

  private createAuthenticationService() {
    switch (this.config.authType) {
      case AuthenticationType.Kerberos: {
        console.info('using kerberos authentication');
        this._authenticationService = new KerberosAuthenticationService();
        break;
      }
      case AuthenticationType.Oauth2: {
        console.info('using oauth2 authentication');
        this._authenticationService = new Oauth2AuthenticationService(this.config.authAttributes);
        break;
      }
      case AuthenticationType.Disabled: {
        console.info('user authentication is disabled');
        this._authenticationService = new DefaultAuthenticationService();
        break;
      }
      default: {
        throw Error('unsupported authentication type');
      }
    }
  }
}
