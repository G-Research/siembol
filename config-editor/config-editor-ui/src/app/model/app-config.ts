import { UserSettings } from "oidc-client";

export interface BuildInfo {
    appName: string;
    appVersion: number;
    buildDate: Date;
    angularVersion: string;
    siembolCompileTimeVersion: string;
}

export enum AuthenticationType {
    Disabled = "disabled",
    Kerberos = "kerberos",
    Oauth2 = "oauth2",
}

export interface AppConfig {
    environment: string;
    serviceRoot: string;
    aboutApp: BuildInfo;
    authType: AuthenticationType,
    authAttributes: Oauth2Attributes | any
}

export interface Oauth2Attributes {
    callbackPath: string;
    expiresIntervalMinimum: number;
    oidcSettings: UserSettings;
}
