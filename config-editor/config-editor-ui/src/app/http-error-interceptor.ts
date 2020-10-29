import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError, timer } from 'rxjs';
import { retryWhen, mergeMap} from 'rxjs/operators';
import { StatusCode } from './commons';

@Injectable({
    providedIn: 'root',
})
export class HttpErrorInterceptor implements HttpInterceptor {
    private readonly MAX_RETRY = 1;
    private readonly TIME_BETWEEN_RETRIES = 1000;
    private readonly STATUS_NOT_RETRY = [StatusCode.BAD_REQUEST, StatusCode.UNAUTHORISED, StatusCode.NOT_FOUND]
    
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req)
            .pipe(
                retryWhen(e =>
                    e.pipe(mergeMap((error, i) => {
                        const retryAttempt = i + 1;
                        if (retryAttempt > this.MAX_RETRY || this.STATUS_NOT_RETRY.includes(error.status)) {
                            return throwError(error);
                        }
                        return timer(this.TIME_BETWEEN_RETRIES)
                    })) 
                )
            );
    }
}
