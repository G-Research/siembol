import { Injectable } from '@angular/core';
import { ConfigData } from '@app/model';
import { TestCase } from '@app/model/test-case';
import { from, Observable, throwError } from 'rxjs';
import { ConfigLoaderService } from './config-loader.service';

@Injectable({
  providedIn: 'root',
})
export class ClipboardService {
  private config: ConfigData;
  private adminConfig: ConfigData;
  private testCase: TestCase;

  get configToBePasted() {
    return this.config;
  }

  get adminConfigToBePasted() {
    return this.adminConfig;
  }

  get testCaseToBePasted() {
    return this.testCase;
  }

  constructor(private configLoader: ConfigLoaderService) {}

  validateConfig(): Observable<boolean> {
    return this.getClipboard()
      .mergeMap(json => {
        return this.configLoader
          .validateConfigJson(json)
          .map(() => {
            return json;
          })
          .catch(e => {
            return throwError(e.error.message);
          });
      })
      .map((json: string) => {
        this.config = JSON.parse(json);
        return true;
      });
  }

  validateTestCase(): Observable<boolean> {
    return this.getClipboard()
      .mergeMap(json => {
        const jsonObj = JSON.parse(json);
        return this.configLoader.validateTestCase(jsonObj).map(() => {
          return jsonObj;
        });
      })
      .map(json => {
        this.testCase = json;
        return true;
      });
  }

  validateAdminConfig(): Observable<boolean> {
    return this.getClipboard()
      .mergeMap(json => {
        return this.configLoader.validateAdminConfigJson(json).map(() => {
          return json;
        });
      })
      .map((json: string) => {
        this.adminConfig = JSON.parse(json);
        return true;
      });
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
