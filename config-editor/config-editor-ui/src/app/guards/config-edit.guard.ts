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

        return this.editorService.configStore.editedConfig$
            .map(x => x !== null);
    }
}