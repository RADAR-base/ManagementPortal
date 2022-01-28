import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
    HttpHandler,
    HttpRequest,
    HttpInterceptor,
    HttpEvent,
} from '@angular/common/http';
import {AuthServerProvider, AuthService, Principal} from '../../shared';

@Injectable()
export class AuthExpiredInterceptor implements HttpInterceptor {

    constructor(
        private auth: AuthService,
        private authServerProvider: AuthServerProvider,
        private principal: Principal,
    ) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            tap({
                error: () => {
                    if (this.principal.isAuthenticated()) {
                        this.auth.authorize(true);
                    } else {
                        this.authServerProvider.logout();
                    }
                }
            })
        );
    }
}
