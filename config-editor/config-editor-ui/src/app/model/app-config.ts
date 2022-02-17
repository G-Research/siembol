import { ServiceContext } from '@app/services/editor.service';
import { UserSettings } from 'oidc-client';

export interface BuildInfo {
  appName: string;
  appVersion: number;
  buildDate: Date;
  angularVersion: string;
}

export enum AuthenticationType {
  Disabled = 'disabled',
  Kerberos = 'kerberos',
  Oauth2 = 'oauth2',
}

export interface AppConfig {
  environment: string;
  serviceRoot: string;
  aboutApp: BuildInfo;
  authType: AuthenticationType;
  authAttributes: Oauth2Attributes | any;
  homeHelpLinks?: HelpLink[];
  managementLinks?: HelpLink[];
  historyMaxSize?: number;
  blockingTimeout?: number;
  useImporters?: boolean;
}

export interface HelpLink {
  title: string;
  icon: string;
  link: string;
}

export interface Oauth2Attributes {
  callbackPath: string;
  expiresIntervalMinimum: number;
  oidcSettings: UserSettings;
}

export const HOME_REGEX = new RegExp('^\/($|home(\/|$))');

export interface ServiceContextMap {
  [serviceName: string]: ServiceContext; 
}

export enum GuardResult {
  SUCCESS = 'success',
  FAILURE = 'failure',
  ROUTE = 'route',
}
