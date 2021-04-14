import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from '@app/services/app-config.service';

@Injectable({
    providedIn: 'root',
  })
export class CredentialsInterceptor implements HttpInterceptor {
    constructor(private appConfig: AppConfigService) {}
    
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const modified = this.appConfig.authenticationService.modifyRequest(req); 
        return next.handle(modified);
    }
}
