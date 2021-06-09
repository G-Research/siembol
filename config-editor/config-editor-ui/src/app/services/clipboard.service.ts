import { Injectable } from '@angular/core';
import { ConfigData } from '@app/model';
import { from, Observable, throwError } from 'rxjs';
import { EditorService } from './editor.service';

@Injectable({
  providedIn: 'root',
})
export class ClipboardService {
  constructor(private editorService: EditorService) {}

  validateConfig(): Observable<boolean> {
    return this.getClipboard()
      .flatMap(json => {
        return this.editorService.configLoader.validateConfigJson(json).map(() => {
          return json;
        });
      })
      .map((json: string) => {
        this.editorService.configStore.updatePastedConfig(JSON.parse(json));
        return true;
      });
  }

  validateTestCase(): Observable<boolean> {
    return this.getClipboard()
      .flatMap(json => {
        const jsonObj = JSON.parse(json);
        return this.editorService.configLoader.validateTestCase(jsonObj).map(() => {
          return jsonObj;
        });
      })
      .map(json => {
        this.editorService.configStore.testService.updatePastedTestCase(json);
        return true;
      });
  }

  validateAdminConfig(): Observable<boolean> {
    return this.getClipboard()
      .flatMap(json => {
        return this.editorService.configLoader.validateAdminConfig(json).map(() => {
          return json;
        });
      })
      .map((json: string) => {
        this.editorService.configStore.updatePastedAdminConfig(JSON.parse(json));
        return true;
      });
  }

  copy(str: any) {
    navigator.clipboard.writeText(JSON.stringify(str));
  }

  private iIsJsonString(str: string): boolean {
    try {
      JSON.parse(str);
    } catch (e) {
      return false;
    }
    return true;
  }

  private getClipboard(): Observable<any> {
    return from(navigator.clipboard.readText()).map(c => {
      if (!this.iIsJsonString(c)) {
        return throwError('Clipboard is not JSON');
      }
      return c;
    });
  }
}
