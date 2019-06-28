import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { Observable } from 'rxjs/Rx';
import { AUTH_TOKEN_COOKIE } from '../constants/common.constants';

@Injectable()
export class AuthServerProvider {

    constructor(
            private http: HttpClient,
            private cookieService: CookieService,
    ) {
    }

    getToken() {
        return this.cookieService.get(AUTH_TOKEN_COOKIE);
    }

    login(credentials): Observable<any> {
        const params = new HttpParams()
                .append('username', credentials.username)
                .append('password',  credentials.password)
                .append('grant_type', 'password');
        const headers = new HttpHeaders()
                .append('Content-Type', 'application/x-www-form-urlencoded')
                .append('Accept', 'application/json');

        const res = this.http.post('oauthserver/oauth/token', {params, headers});
        res.subscribe((data: any) => {
            console.log('data ', data);
            const expiredAt = new Date();
            expiredAt.setSeconds(expiredAt.getSeconds() + data.expires_in);
            data.expires_at = expiredAt.getTime();
            this.cookieService.delete(AUTH_TOKEN_COOKIE);
            this.cookieService.set(AUTH_TOKEN_COOKIE, data);
            // return data;
        });
        return res;
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
            this.cookieService.deleteAll();
            observer.complete();
        });
    }
}
