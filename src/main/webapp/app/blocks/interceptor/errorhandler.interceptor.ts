import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest
} from '@angular/common/http';

import { EventManager } from '../../shared/util/event-manager.service';

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {

    constructor(private eventManager: EventManager) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            tap({
                error: (err: HttpErrorResponse) => {
                    if (!(err.status === 401 && (err.message === '' || (err.url && err.url.includes('/api/account'))))) {
                        this.eventManager.broadcast({
                            name: 'managementPortalApp.httpError',
                            content: err
                        });
                    }
                }
        }));
    }
}
