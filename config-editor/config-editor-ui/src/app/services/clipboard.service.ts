import { Injectable } from '@angular/core';
import { ConfigData } from '@app/model';
import { Observable, throwError } from 'rxjs';
import { EditorService } from './editor.service';

@Injectable({
  providedIn: 'root',
})
export class ClipboardService {
  constructor(private editorService: EditorService) {}

  async validateConfig(): Promise<Observable<boolean>> {
    const json = await this.getClipboard();
    return this.editorService.configLoader.validateConfigJson(json).map(() => {
      this.editorService.configStore.updatePastedConfig(JSON.parse(json));
      return true;
    });
  }

  async validateTestCase(): Promise<Observable<boolean>> {
    const json = await this.getClipboard();
    const jsonObj = JSON.parse(json);
    return this.editorService.configLoader.validateTestCase(jsonObj).map(() => {
      this.editorService.configStore.testService.updatePastedTestCase(jsonObj);
      return true;
    });
  }

  async validateAdminConfig(): Promise<Observable<boolean>> {
    const json = await this.getClipboard();
    return this.editorService.configLoader.validateAdminConfigJson(json).map(() => {
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

  private async getClipboard(): Promise<any> {
    const str = await navigator.clipboard.readText();
    if (!this.iIsJsonString(str)) {
      return Promise.reject('Clipboard is not JSON');
    }
    return str;
  }
}
