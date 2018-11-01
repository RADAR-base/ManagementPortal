import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { Observable } from 'rxjs/Rx';
import { AUTH_TOKEN_COOKIE } from '../constants/common.constants';

@Injectable()
export class AuthServerProvider {

    constructor(
            private http: Http,
            private cookieService: CookieService,
    ) {
    }

    getToken() {
        return this.cookieService.getObject(AUTH_TOKEN_COOKIE);
    }

    login(credentials): Observable<any> {
        const params = 'username=' + encodeURIComponent(credentials.username) + '&password=' +
                encodeURIComponent(credentials.password) + '&grant_type=password';
        const headers = new Headers({
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json',
        });

        return this.http.post('oauthserver/oauth/token', params, {headers})
                .map((response) => {
                    const data = response.json();
                    const expiredAt = new Date();
                    expiredAt.setSeconds(expiredAt.getSeconds() + data.expires_in);
                    data.expires_at = expiredAt.getTime();
                    this.cookieService.remove(AUTH_TOKEN_COOKIE);
                    this.cookieService.putObject(AUTH_TOKEN_COOKIE, data);
                    return data;
                });
    }

    sendRefreshTokenRequest(): Observable<Response> {
        const headers = new Headers({
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json',
        });
        const params = 'grant_type=refresh_token';
        return this.http.post('oauthserver/oauth/token', params, {headers});
    }

    logout(): Observable<any> {
        return new Observable(observer => {
            this.http.post('api/logout', {});
            // revoke rft token
            const headers = new Headers({
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json',
            });
            // remove other cookies
            this.http.delete('oauthserver/oauth/token', {headers});
            this.cookieService.removeAll();
            observer.complete();
        });
    }
}
