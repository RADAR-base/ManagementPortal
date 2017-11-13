import { Injectable, Inject } from '@angular/core';
import { Http, Response, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { LocalStorageService } from 'ng2-webstorage';

import { Base64 } from 'ng-jhipster';
import {CookieService} from "angular2-cookie/core";
import {AUTH_TOKEN_COOKIE} from "../constants/common.constants";

@Injectable()
export class AuthServerProvider {

    constructor(
        private http: Http,
        private cookieService : CookieService,
    ) {}

    getToken() {
        return this.cookieService.getObject(AUTH_TOKEN_COOKIE);
    }

    login(credentials): Observable<any> {
        const data = 'username=' +  encodeURIComponent(credentials.username) + '&password=' +
            encodeURIComponent(credentials.password) + '&grant_type=password';
        const headers = new Headers ({
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json',
        });

        return this.http.post('oauthserver/oauth/token', data, {
            headers: headers
        }).map(authSuccess.bind(this));

        function authSuccess(resp) {
            const response = resp.json();
            const expiredAt = new Date();
            expiredAt.setSeconds(expiredAt.getSeconds() + response.expires_in);
            response.expires_at = expiredAt.getTime();
            this.cookieService.remove(AUTH_TOKEN_COOKIE);
            this.cookieService.putObject(AUTH_TOKEN_COOKIE, response);
            return response;
        }
    }

    sendRefreshTokenRequest(): Observable<Response> {
        const headers = new Headers({
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json'
        });
        const refreshData = 'grant_type=refresh_token';
        return this.http.post('oauthserver/oauth/token', refreshData, {
            headers: headers
        });
    }

    logout(): Observable<any> {
        return new Observable(observer => {
            this.http.post('api/logout', {});
            // revoke rft token
            const headers = new Headers({
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            });
            // remove other cookies
            this.http.delete('oauthserver/oauth/token', {headers: headers});
            this.cookieService.removeAll();
            observer.complete();
        });
    }
}
