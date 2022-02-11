import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Params, Router } from '@angular/router';
import { EditorService } from '../services/editor.service';
import { map, mergeMap, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConfigEditGuard implements CanActivate {
  constructor(
    private editorService: EditorService, 
    private router: Router
    ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (!this.editorService.configStore || !this.editorService.configStore.editedConfig$) {
      return false;
    }
    const result = this.setConfig(route.queryParams);
    if (result instanceof Observable) {
      return result.pipe(mergeMap((success: boolean) => {
        if (success) {
          this.router.navigate([this.editorService.serviceName, 'edit'], { 
            queryParams: { configName: route.queryParams.newConfigName },
          })
        }
        return of(false);
        }
      ));
    }
    
    if (result === false) {
      return false;
    }
    return this.editorService.configStore.editedConfig$.pipe(map(x => x !== null));
    
    
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

  private setConfig(params: Params): Observable<boolean> | boolean {
    if (params.cloneConfig && params.newConfigName && params.withTestCases && params.fromService) {
      if (params.fromService !== this.editorService.serviceName) {
        return this.editorService.configStore.setClonedConfigAndTestsOtherService(
          params.cloneConfig, params.newConfigName, params.withTestCases, params.fromService
        );
      }
      return this.editorService.configStore.setClonedConfigAndTests(params.cloneConfig, params.newConfigName, params.withTestCases);
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
