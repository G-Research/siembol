import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate } from '@angular/router';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { combineLatest, Observable } from 'rxjs';
import { filter, map, take, tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
  })
export class ConfigStoreGuard implements CanActivate {

    constructor(private store: Store<fromStore.State>) { }

    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
        const id = parseInt(route.params['id'], 10);
        const serviceName = route.parent.url[0].path;

        return isNaN(id)
            ? false
            : combineLatest(this.store.select(fromStore.getBootstrapped), this.store.select(fromStore.getConfigs)).pipe(

            tap(([bootstrapped, configs]) => {
                if (bootstrapped !== serviceName) {
                    this.store.dispatch(new fromStore.Bootstrap(serviceName));
                }
            }),
            filter(([bootstrapped, configs]) => bootstrapped === serviceName),
            map(([isBootstrapped, configs]) => id < configs.length),
            take(1));
    }
}
