import { Observable, of } from 'rxjs';
import { HttpRequest } from '@angular/common/http';

export interface IAuthenticationService {
  loadedUser: boolean;
  loadedUser$: Observable<boolean>;
  redirectedUrl: string;
  loadUser(): void;
  modifyRequest(req: HttpRequest<any>): HttpRequest<any>;
  isCallbackUrl(url: string): boolean;
}

export class DefaultAuthenticationService implements IAuthenticationService {
  private readonly REDIRECTED_URI_KEY = 'siembol_redirected_url';

  // eslint-disable-next-line no-unused-vars
  isCallbackUrl(url: string): boolean {
    return false;
  }

  get redirectedUrl(): string {
    return localStorage.getItem(this.REDIRECTED_URI_KEY);
  }

  set redirectedUrl(uri: string) {
    localStorage.setItem(this.REDIRECTED_URI_KEY, uri);
  }

  get loadedUser() {
    return true;
  }

  get loadedUser$() {
    return of(true);
  }

  loadUser(): void {}

  modifyRequest(req: HttpRequest<any>): HttpRequest<any> {
    return req;
  }
}

export class KerberosAuthenticationService extends DefaultAuthenticationService {
  modifyRequest(req: HttpRequest<any>): HttpRequest<any> {
    return req.clone({ withCredentials: true });
  }
}
