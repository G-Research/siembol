import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';

import { Observable } from 'rxjs';
import { AppConfigService } from '@app/services/app-config.service';

@Injectable({
    providedIn: 'root',
})

export class AuthGuard implements CanActivate {

    constructor(private appConfig: AppConfigService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
        const authProvider = this.appConfig.authenticationService;

        if (authProvider.loadedUser) {
            return true;
        }

        if (!authProvider.isCallbackUrl(state.url)) {
            authProvider.redirectedUrl = state.url;
            authProvider.loadUser();
            return false;
        }

        return authProvider.loadedUser$;
    }
}
