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

    requestIntercept(options?: RequestOptionsArgs): RequestOptionsArgs {
        // retrieve token from cookie
        const authServerProvider = this.injector.get(AuthServerProvider);
        const token: any = authServerProvider.getToken();
        if (token && token.expires_at && token.expires_at > new Date().getTime()) {
            options.headers.append('Authorization', 'Bearer ' + token.access_token);
        }
        return options;
    }

    responseIntercept(observable: Observable<Response>): Observable<Response> {
        return observable; // by pass
    }

}
