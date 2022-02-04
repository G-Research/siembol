import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate } from '@angular/router';

import { EditorService } from '../services/editor.service';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class EditorServiceGuard implements CanActivate {
  constructor(private editorService: EditorService) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const serviceName = route.parent.url[0].path;
    if (this.editorService.serviceName === serviceName && !this.editorService.adminMode) {
      return true;
    }

    return this.editorService.createConfigServiceContext(serviceName)
      .pipe(map(x => this.editorService.setServiceContext(x)));
  }
}
