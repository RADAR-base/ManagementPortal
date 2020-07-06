import { Injector } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import {
    HttpHandler,
    HttpRequest,
    HttpInterceptor,
    HttpEvent,
    HttpErrorResponse
} from '@angular/common/http';
import {AuthServerProvider, AuthService, Principal} from '../../shared';

export class AuthExpiredInterceptor implements HttpInterceptor {

    constructor(private injector: Injector) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).do((event: HttpEvent<any>) => {}, (err: any) => {
            if (err instanceof HttpErrorResponse) {
                const principal: Principal = this.injector.get(Principal);

                if (principal.isAuthenticated()) {
                    const auth: AuthService = this.injector.get(AuthService);
                    auth.authorize(true);
                } else {
                    const authServerProvider: AuthServerProvider = this.injector.get(AuthServerProvider);
                    authServerProvider.logout();
                }
            }
        });
    }
}
