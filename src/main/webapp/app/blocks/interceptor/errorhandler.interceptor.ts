import { Injectable, Injector } from '@angular/core';
import { Router } from '@angular/router';
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
import { AuthService } from "../../shared";

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {

    constructor(private eventManager: EventManager, private injector: Injector, private router: Router) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            tap(
                () => { },
                (err: HttpErrorResponse) => {
                    if (err.status === 401 && request.url === 'api/account') {
                        // If the account endpoint says 401, the user is no longer authenticated.
                        // Only redirect if we're not already on the home page (to allow initial page load)
                        const currentUrl = this.router.url;
                        const isHomePage = currentUrl === '/' || currentUrl === '' || currentUrl.startsWith('/?') || currentUrl.startsWith('/#/');

                        if (!isHomePage) {
                            // User was authenticated but session expired, redirect to login
                            this.injector.get(AuthService).resetAuthentication(true);
                            return;
                        }
                        // On home page, don't do anything - let the catchError in principal.reset() handle it
                        return;
                    }
                    if (err.status === 409 && request.method === 'DELETE') {
                        // don't report error about this.
                        return;
                    }
                    this.eventManager.broadcast({
                        name: 'managementPortalApp.httpError',
                        content: err
                    });
                }),
        );
    }
}
