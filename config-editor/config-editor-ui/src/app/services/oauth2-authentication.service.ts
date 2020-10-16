import { DefaultAuthenticationService } from "./authentication.service";
import { UserManager } from 'oidc-client';
import { BehaviorSubject, from, Observable } from "rxjs";
import { HttpRequest } from "@angular/common/http";
import { Oauth2Attributes } from "@app/model/app-config";

export class Oauth2AuthenticationService extends DefaultAuthenticationService {
    private callbackPath: string;
    private token: string;
    private userManager: UserManager;
    private _loadedUser: boolean;

    constructor(authAttributes: Oauth2Attributes) {
        super();
        this.callbackPath = authAttributes.callbackPath;
        this.userManager = new UserManager(authAttributes.oidcSettings);
        this.userManager.getUser().then(user => {
            if (user
                && !user.expired) {
                this.token = user.access_token;
                this._loadedUser = true;
            }
        });
    }

    modifyRequest(req: HttpRequest<any>): HttpRequest<any> {
        if (!this.loadedUser) {
            return req;
        }

        return req.clone({
            setHeaders: {
                Authorization: `Bearer ${this.token}`
            }
        });
    }

    get loadedUser() {
        return this._loadedUser;
    }

    get loadedUser$() {
        return this._loadedUser
            ? Observable.of(true)
            : this.completeAuthentication();
    }


    loadUser(): void {
        this.userManager.signinRedirect();
    }

    private completeAuthentication(): Observable<boolean> {
        return from(this.userManager.signinRedirectCallback().catch())
            .map(user => {
                if (user) {
                    this.token = user.access_token;
                    this._loadedUser = true;
                    return true;
                }
                return false;
            })
    }

    isCallbackUrl(url: string): boolean {
        return url && url.startsWith(this.callbackPath);
    }
}
