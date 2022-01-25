import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';

import { Observable } from 'rxjs';
import { AppService } from '../services/app.service';

@Injectable({
    providedIn: 'root',
})

export class AppInitGuard implements CanActivate {

    constructor(private appService: AppService) { }

    canActivate(): Observable<boolean> | boolean {
        if (this.appService.loaded) {
            return true;
        }

        return this.appService.createAppContext().map(x => this.appService.setAppContext(x));
    }
}