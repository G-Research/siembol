import { Injectable } from '@angular/core';
import { InputError, Type } from '@app/model/config-model';
import { ConfigStoreState } from '@app/model/store-state';
import { BehaviorSubject, from, map, catchError, mergeMap, Observable, throwError } from 'rxjs';
import { ConfigLoaderService } from './config-loader.service';
import { ConfigStoreStateBuilder } from './store/config-store-state.builder';

@Injectable({
  providedIn: 'root',
})
export class ClipboardStoreService {
  constructor(private configLoader: ConfigLoaderService, private store: BehaviorSubject<ConfigStoreState>) {}

  validateConfig(type: Type): Observable<boolean> {
    return this.getClipboard().pipe(
      mergeMap(s => {
        const json = JSON.parse(s);
        return this.validateType(type, json)
          .pipe(
            map(() => json),
          );
      }),
      map((json: string) => {
        this.updatePastedConfig(json);
        return true;
      }));
  }

  copyFromClipboard(str: any) {
    navigator.clipboard.writeText(JSON.stringify(str, null, 2));
  }

  updatePastedConfig(config: any) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue()).pastedConfig(config).build();
    this.store.next(newState);
  }

  private validateType(type: Type, json: any): Observable<any> {
    switch (type) {
      case Type.CONFIG_TYPE: return this.configLoader.validateConfig(json);
      case Type.ADMIN_TYPE: return this.configLoader.validateAdminConfig(json);
      default: return this.configLoader.validateTestCase(json);
    }
  }

  private isJsonString(str: string): boolean {
    try {
      JSON.parse(str);
    } catch (e) {
      return false;
    }
    return true;
  }

  private getClipboard(): Observable<any> {
    return from(navigator.clipboard.readText()).pipe(map(c => {
      if (!this.isJsonString(c)) {
        throw new InputError('Clipboard is not JSON');
      }
      return c;
    }));
  }
}
