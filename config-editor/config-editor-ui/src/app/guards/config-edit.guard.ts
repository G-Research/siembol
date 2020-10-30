import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate } from '@angular/router';

import { EditorService } from '../services/editor.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})

export class ConfigEditGuard implements CanActivate {

    constructor(private editorService: EditorService) { }

    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
        if (!this.editorService.configStore
            || !this.editorService.configStore.editedConfig$) {
            return false;
        }

        const configName = route.queryParams.configName;
        const testCaseName = route.queryParams.testCaseName;
        const newConfig = route.queryParams.newConfig;
        const cloneConfig = route.queryParams.cloneConfig;
        const newTestCase = route.queryParams.newTestCase;
        const cloneTestCase = route.queryParams.cloneTestCase;

        if (cloneConfig) {
            this.editorService.configStore.setEditedClonedConfigByName(cloneConfig);
        } else if (newConfig) {
            this.editorService.configStore.setEditedConfigNew();
        } else if (configName) {
            if (!this.editorService.configStore.setEditedConfigAndTestCaseByName(configName, testCaseName)) {
                return false;
            }
            if (cloneTestCase) {
                this.editorService.configStore.testService.setEditedClonedTestCaseByName(cloneTestCase);
            } else if (newTestCase) {
                this.editorService.configStore.testService.setEditedTestCaseNew();
            }
        }

        return this.editorService.configStore.editedConfig$
            .map(x => x !== null);
    }

}