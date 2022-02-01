import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Params } from '@angular/router';

import { EditorService } from '../services/editor.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConfigEditGuard implements CanActivate {
  constructor(private editorService: EditorService) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (!this.editorService.configStore || !this.editorService.configStore.editedConfig$) {
      return false;
    }
    if (!this.setConfig(route.queryParams)) {
      return false;
    }
    return this.editorService.configStore.editedConfig$.map(x => x !== null);
  }

  private setTestCase(params: Params) {
    if (params.cloneTestCase) {
      this.editorService.configStore.testService.setEditedClonedTestCaseByName(params.cloneTestCase);
    } else if (params.newTestCase) {
      this.editorService.configStore.testService.setEditedTestCaseNew();
    } else if (params.pasteTestCase) {
      this.editorService.configStore.testService.setEditedPastedTestCaseNew();
    }
  }

  private setConfig(params: Params): boolean {
    if (params.cloneConfig) {
      this.editorService.configStore.setEditedClonedConfigByName(params.cloneConfig);
    } else if (params.newConfig) {
      this.editorService.configStore.setEditedConfigNew();
    } else if (params.pasteConfig) {
      this.editorService.configStore.setNewEditedPastedConfig();
    } else if (params.configName) {
      if (!this.editorService.configStore.setEditedConfigAndTestCaseByName(params.configName, params.testCaseName)) {
        return false;
      }
      this.setTestCase(params);
    }
    return true;
  }
}
