import { DefaultAuthenticationService } from "./authentication.service";
import { User, UserManager } from 'oidc-client';
import { from, Observable } from "rxjs";
import { HttpRequest } from "@angular/common/http";
import { Oauth2Attributes } from "@app/model/app-config";

export class Oauth2AuthenticationService extends DefaultAuthenticationService {
    private callbackPath: string;
    private expiresIntervalMinimum: number;
    private userManager: UserManager;
    private user: User = null;

    constructor(authAttributes: Oauth2Attributes) {
        super();
        this.callbackPath = authAttributes.callbackPath;
        this.expiresIntervalMinimum = authAttributes.expiresIntervalMinimum ? authAttributes.expiresIntervalMinimum : 0;
        this.userManager = new UserManager(authAttributes.oidcSettings);
        this.userManager.getUser().then(user => {
            if (user && this.hasUserFreshToken(user)) {
                this.user = user;
            }
        });
    }

    modifyRequest(req: HttpRequest<any>): HttpRequest<any> {
        if (!this.user) {
            return req;
        }

        return req.clone({
            setHeaders: {
                Authorization: `Bearer ${this.user.access_token}`
            }
        });
    }

    get loadedUser() {
        return this.user !== null && this.hasUserFreshToken(this.user);
    }

    get loadedUser$() {
        return this.hasUserFreshToken(this.user)
            ? Observable.of(true)
            : this.completeAuthentication();
    }


    loadUser(): void {
        this.userManager.signinRedirect();
    }

    isCallbackUrl(url: string): boolean {
        return url && url.startsWith(this.callbackPath);
    }

    private completeAuthentication(): Observable<boolean> {
        return from(this.userManager.signinRedirectCallback().catch())
            .map(user => {
                if (this.hasUserFreshToken(user)) {
                    this.user = user;
                    return true;
                }
                return false;
            })
    }

    private hasUserFreshToken(user: User): boolean {
        if (!user) {
            return false;
        }

        return user.expires_in > this.expiresIntervalMinimum;
    }
}
