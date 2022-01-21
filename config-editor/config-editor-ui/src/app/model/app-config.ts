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
  icon?: string;
  link: string;
}

export interface ManagementAction {
  icon: string;
  title: string;
  funct: () => void;
}

export interface Oauth2Attributes {
  callbackPath: string;
  expiresIntervalMinimum: number;
  oidcSettings: UserSettings;
}
