import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthServerProvider, TokenData} from '../../shared';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private authServerProvider: AuthServerProvider,
    ) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const tokenString: string = this.authServerProvider.getToken();
        if (!!tokenString) {
            const token: TokenData = JSON.parse(tokenString);
            if (!!token && token.expires_at && token.expires_at > new Date().getTime()) {
                const newReq = request.clone({
                    setHeaders: {
                        Authorization: 'Bearer ' + token.access_token
                    }
                });
                return next.handle(newReq);
            }
        }
        return next.handle(request);
    }
}
