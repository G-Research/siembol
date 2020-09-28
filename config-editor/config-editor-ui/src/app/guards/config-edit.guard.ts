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
        if (configName || testCaseName) {
            if (!this.editorService.configStore.setEditedConfigAndTestCaseByName(configName, testCaseName)) {
                return false;
            }
        }

        return this.editorService.configStore.editedConfig$
            .map(x => x !== null);
    }

}