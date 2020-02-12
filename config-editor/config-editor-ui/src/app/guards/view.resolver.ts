import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { Observable } from 'rxjs';
import { filter, map, take, tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
  })
export class ViewResolver implements Resolve<boolean> {
    constructor(private store: Store<fromStore.State>) { }

    resolve(route: ActivatedRouteSnapshot): Observable<boolean> {
        let serviceName = '';
        if (route.parent.url[0] === undefined) {
            return of(false);
        }
        serviceName = route.parent.url[0].path;

        return this.store.select(fromStore.getBootstrapped).pipe(
            tap(bootstrapped => {
                if (bootstrapped !== serviceName) {
                    this.store.dispatch(new fromStore.Bootstrap(serviceName));
                }
            }),
            map(bootstrapped => bootstrapped === serviceName),
            filter(isBootstrapped => isBootstrapped),
            take(1));
    }
}
