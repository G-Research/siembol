import { Action } from '@ngrx/store';
import { State } from 'app/store';
import * as classifications from 'app/store/editor.reducer';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operator/map';

export class MockStore<T> extends BehaviorSubject<T> {

    private expectFunc: (action: Action) => void;

    constructor(initialState2: T) {
        super(initialState2);
    }

    dispatch = (action: Action): void => {
        if (this.expectFunc) {
            this.expectFunc(action);
        }
    }

    select = <R>(pathOrMapFn: any): Observable<R> => map.call(this, pathOrMapFn);

    get state(): T {
        return this.getValue();
    }

    updateState(state: T): void {
        this.next(state);
    }

    setExpect(func: (action: Action) => void): void {
        this.expectFunc = func;
    }
}

export const initialState: State = {
    editor: classifications.initialState,
    routerReducer: undefined,
};
