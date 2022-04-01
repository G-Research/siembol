import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate } from '@angular/router';

import { EditorService } from '../services/editor.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SearchGuard implements CanActivate {
  constructor(private editorService: EditorService) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    this.editorService.configStore.updateSearchTermAndFilters(route.queryParamMap);
    return true;
  }
}
