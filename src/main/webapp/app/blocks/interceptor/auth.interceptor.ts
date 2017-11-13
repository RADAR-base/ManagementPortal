import { Observable } from 'rxjs/Observable';
import { RequestOptionsArgs, Response } from '@angular/http';
import { LocalStorageService, SessionStorageService } from 'ng2-webstorage';
import { HttpInterceptor } from 'ng-jhipster';
import {Injector} from "@angular/core";
import {CookieService} from "angular2-cookie/core";
import {AUTH_TOKEN_COOKIE} from "../../shared/constants/common.constants";

export class AuthInterceptor extends HttpInterceptor {

    constructor(
        private injector: Injector,
    ) {
        super();
    }

    requestIntercept(options?: RequestOptionsArgs): RequestOptionsArgs {
        // retrieve token from cookie
        const cookieService = this.injector.get(CookieService);
        const token : any = cookieService.getObject(AUTH_TOKEN_COOKIE);
        if (token && token.expires_at && token.expires_at > new Date().getTime()) {
            options.headers.append('Authorization', 'Bearer ' + token.access_token);
        }
        return options;
    }

    responseIntercept(observable: Observable<Response>): Observable<Response> {
        return observable; // by pass
    }

}
