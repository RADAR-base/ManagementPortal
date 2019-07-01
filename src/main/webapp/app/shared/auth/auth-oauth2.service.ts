import { Injectable } from '@angular/core';
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
        const body = new HttpParams()
                .append('username', credentials.username)
                .append('password',  credentials.password)
                .append('grant_type', 'password');
        const headers = new HttpHeaders()
                .append('Content-Type', 'application/x-www-form-urlencoded')
                .append('Accept', 'application/json');

        const res = this.http.post('oauthserver/oauth/token', body, {headers, observe: 'response'});

        res.subscribe((resp: any) => {
            console.log('data ', resp.body);
            const data: TokenData = resp.body;
            const expiredAt = new Date();
            expiredAt.setSeconds(expiredAt.getSeconds() + data.expires_in);
            data.expires_at = expiredAt.getTime();
            this.cookieService.delete(AUTH_TOKEN_COOKIE);
            this.cookieService.set(AUTH_TOKEN_COOKIE, JSON.stringify(data));
            // return data;
        });
        return res;
    }

    sendRefreshTokenRequest(): Observable<any> {
        const headers = new HttpHeaders()
                .append('Content-Type', 'application/x-www-form-urlencoded')
                .append('Accept', 'application/json');
        const params = 'grant_type=refresh_token';
        return this.http.post('oauthserver/oauth/token', params, {headers});
    }

    logout(): Observable<any> {
        return new Observable(observer => {
            this.http.post('api/logout', {});
            // revoke rft token
            const headers = new HttpHeaders()
                    .append('Content-Type', 'application/x-www-form-urlencoded')
                    .append('Accept', 'application/json');
            // remove other cookies
            this.http.delete('oauthserver/oauth/token', {headers});
            this.cookieService.deleteAll();
            observer.complete();
        });
    }
}

export class TokenData {
    access_token?: string;
    token_type?: string;
    expires_in?: number;
    scope?: string[];
    sub: string;
    sources: string[];
    grant_type: string[];
    roles: string[];
    iss: string[];
    iat: number;
    expires_at: number;
}
