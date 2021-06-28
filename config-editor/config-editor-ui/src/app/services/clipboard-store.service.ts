import { Injectable } from '@angular/core';
import { Type } from '@app/model/config-model';
import { from, Observable, throwError } from 'rxjs';
import { ConfigLoaderService } from './config-loader.service';

@Injectable({
  providedIn: 'root',
})
export class ClipboardStoreService {
  private config: any;

  get configToBePasted() {
    return this.config;
  }

  constructor(private configLoader: ConfigLoaderService) {}

  validateConfig(type: Type): Observable<boolean> {
    return this.getClipboard()
      .mergeMap(s => {
        const json = JSON.parse(s);
        return this.validateType(type, json)
          .map(() => {
            return json;
          })
          .catch(e => {
            return throwError(e.error.message);
          });
      })
      .map((json: string) => {
        this.config = json;
        return true;
      });
  }

  validateType(type: Type, json: any): Observable<any> {
    if (type === Type.CONFIG_TYPE) {
      return this.configLoader.validateConfig(json);
    } else if (type === Type.ADMIN_TYPE) {
      return this.configLoader.validateAdminConfig(json);
    } else {
      return this.configLoader.validateTestCase(json);
    }
  }

  copy(str: any) {
    navigator.clipboard.writeText(JSON.stringify(str));
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
    return from(navigator.clipboard.readText()).map(c => {
      if (!this.isJsonString(c)) {
        throw Error('Clipboard is not JSON');
      }
      return c;
    });
  }
}
