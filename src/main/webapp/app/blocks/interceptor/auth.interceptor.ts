import { Injector } from '@angular/core';
import { RequestOptionsArgs, Response } from '@angular/http';
import { HttpInterceptor } from 'ng-jhipster';
import { Observable } from 'rxjs/Observable';
import { AuthServerProvider } from '../../shared/auth/auth-oauth2.service';

export class AuthInterceptor extends HttpInterceptor {

    constructor(
            private injector: Injector,
    ) {
        super();
    }

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
