import { Location } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { RouterStateUrl } from '@app/app-routing';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { ROUTER_NAVIGATION, RouterNavigationAction } from '@ngrx/router-store';
import { Action, Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';
import * as ProductActions from './editor.actions';
import { State } from './editor.reducer';
import * as RouterActions from './router-actions';

@Injectable({
    providedIn: 'root',
  })
export class RouterEffects {

  @Effect()
  configSelection$: Observable<Action> = this.actions$.pipe(
    ofType<RouterNavigationAction<RouterStateUrl>>(ROUTER_NAVIGATION),
    map(route => route.payload.routerState),
    withLatestFrom(this.store.select(fromStore.getBootstrapped), this.store.select(fromStore.getConfigs)),
    filter(([route, isBootstrapped, configs]) =>
      route.url.includes('edit') && (!isBootstrapped || parseInt(route.params['id'], 10) < configs.length)),
    switchMap(([route]) => of(new ProductActions.SelectConfig(parseInt(route.params['id'], 10)))),
    catchError(e => this.errorHandler(e, 'Failed to navigate to ', of()))
  );

  @Effect({ dispatch: false })
  navigate$ = this.actions$.pipe(
    ofType(RouterActions.GO),
    map((action: RouterActions.Go) => action.payload),
    tap(({ path, query: queryParams, extras }) =>
      this.router.navigate(path, { queryParams, ...extras })
    )
  );

  @Effect({ dispatch: false })
  navigateBack$ = this.actions$.pipe(
    ofType(RouterActions.BACK),
    tap(() => this.location.back())
  );

  @Effect({ dispatch: false })
  navigateForward$ = this.actions$.pipe(
    ofType(RouterActions.FORWARD),
    tap(() => this.location.forward())
  );


  @Effect()
  searchNavigation$: Observable<Action> = this.navigationOnQueryParam(
    'search',
    (state) => state.searchTerm,
    (route, paramKey, state) => {
      return of(new ProductActions.SearchConfig(route.queryParams[paramKey]));
    },
    'Failed to set search term'
  );

  constructor(
    private actions$: Actions,
    private store: Store<fromStore.State>,
    private router: Router,
    private location: Location
  ) {}

  navigationOnQueryParam(queryParamKey: string,
    getStateValue: ((state: State) => string),
    nextAction: ((route: RouterStateUrl, paramKey: string, state: State) => Observable<Action>),
    errorMessage: string): Observable<Action> {
    return this.actions$.pipe(
      ofType<RouterNavigationAction<RouterStateUrl>>(ROUTER_NAVIGATION),
      map(route => route.payload.routerState),
      withLatestFrom(of(queryParamKey), this.store.select(fromStore.getEditorState)),
      filter(([route, paramKey, state]) => paramKey in route.queryParams && route.queryParams[paramKey] !== getStateValue(state)),
      switchMap(([route, paramKey, state]) => nextAction(route, paramKey, state)),
      catchError(e =>
        this.errorHandler(e, errorMessage, of())
      )
    );
  }

  errorHandler(error: any, description: string, action$: Observable<Action>): Observable<Action> {
    return action$;
  }
}
