import { Injectable } from '@angular/core';
import { Config } from '@app/model';
import { Observable, Subscription } from 'rxjs';
import { EditorService } from './editor.service';

@Injectable({
  providedIn: 'root',
})
export class ClipboardService {
  toPaste: Config;

  constructor(private editorService: EditorService) {}

  async validateClipboard(): Promise<Observable<boolean>> {
    const json = await navigator.clipboard.readText();
    return this.editorService.configLoader.validateConfigJson(json).map(() => {
      this.toPaste = JSON.parse(json);
      return true;
    });
  }
}
