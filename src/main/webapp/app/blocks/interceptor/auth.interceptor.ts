import {Injector} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthServerProvider, TokenData} from '../../shared';

export class AuthInterceptor implements HttpInterceptor {

    constructor(
            private injector: Injector,
    ) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        const authServerProvider = this.injector.get(AuthServerProvider);
        const tokenString : string = authServerProvider.getToken();
        if (!!tokenString) {
            const token: TokenData = JSON.parse(tokenString);
            if (!!token && token.expires_at && token.expires_at > new Date().getTime()) {
                request = request.clone({
                    setHeaders: {
                        Authorization: 'Bearer ' + token.access_token
                    }
                });
            }
        }
        return next.handle(request);
    }
}
