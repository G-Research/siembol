import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map, take, tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
  })
export class RepoResolver implements Resolve<boolean> {

  constructor(private store: Store<fromStore.State>) { }

  resolve(): Observable<boolean> {
    return this.store.select(fromStore.getRepositoryLinks).pipe(
      tap(repositoryLinks => {
        if (!repositoryLinks) {
          this.store.dispatch(new fromStore.LoadRepositories());
        }
      }),
      map(repositoryLinks => true),
      take(1));
  }
}
