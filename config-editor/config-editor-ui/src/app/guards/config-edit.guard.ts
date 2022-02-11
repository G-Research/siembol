import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Params, Router } from '@angular/router';
import { EditorService } from '../services/editor.service';
import { map, mergeMap, Observable, of } from 'rxjs';
import { GuardResult } from '@app/model/app-config';

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
    return this.setConfig(route.queryParams).pipe(mergeMap((result: GuardResult) => {
      switch(result) {
        case (GuardResult.ROUTE): {
          this.router.navigate([this.editorService.serviceName, 'edit'], { 
            queryParams: { configName: route.queryParams.newConfigName },
          });
          break;
        } case (GuardResult.SUCCESS): {
          return this.editorService.configStore.editedConfig$.pipe(map(x => x !== null));
        }
        default: {
          return of(false);
        }
      }
    }));
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

  private setConfig(params: Params): Observable<GuardResult> {
    if (params.cloneConfig && params.newConfigName && params.withTestCases && params.fromService) {
      return this.setClonedConfig(params);
    } else if (params.newConfig) {
      this.editorService.configStore.setEditedConfigNew();
    } else if (params.pasteConfig) {
      this.editorService.configStore.setNewEditedPastedConfig();
    } else if (params.configName) {
      if (!this.editorService.configStore.setEditedConfigAndTestCaseByName(params.configName, params.testCaseName)) {
        return of(GuardResult.FAILURE);
      }
      this.setTestCase(params);
    }
    return of(GuardResult.SUCCESS);
  }

  private setClonedConfig(params: Params): Observable<GuardResult> {
    let result: Observable<boolean>;
    if (params.fromService !== this.editorService.serviceName) {
      result =  this.editorService.configStore.setClonedConfigAndTestsFromOtherService(
        params.cloneConfig, params.newConfigName, params.withTestCases, params.fromService
      );
    } else {
      result = this.editorService.configStore.setClonedConfigAndTests(
        params.cloneConfig, params.newConfigName, params.withTestCases
      );
    }
    return result.pipe(map(
      success => {
        if (success === true) {
          return GuardResult.ROUTE;
        } 
        return GuardResult.FAILURE;
      }
    ));
  }
}
