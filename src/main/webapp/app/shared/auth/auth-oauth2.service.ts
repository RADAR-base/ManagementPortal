import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { first, tap } from 'rxjs/operators';
import { map, switchMap } from "rxjs/operators";

@Injectable({ providedIn: 'root' })
export class AuthServerProvider {

    constructor(
            private http: HttpClient,
    ) {
    }

    login(credentials): Observable<any> {
        const body = new HttpParams()
                .append('client_id', 'ManagementPortalapp')
                .append('username', credentials.username)
                .append('password',  credentials.password)
                .append('grant_type', 'password');
        const headers = new HttpHeaders()
                .append('Content-Type', 'application/x-www-form-urlencoded')
                .append('Accept', 'application/json');

        return this.http.post('oauth/token', body, {headers, observe: 'body'}, )
            .pipe(
              switchMap((tokenData: TokenData) => {
                const authHeaders = new HttpHeaders()
                    .append('Authorization', 'Bearer ' + tokenData.access_token);
                return this.http.post('api/login', null, {
                  headers: authHeaders, observe: 'body', withCredentials: true
                });
              }),
            );
    }

    requestOtpCode(credentials) :Observable<any> {
        const headers = new HttpHeaders().append('Accept', 'application/json');
        return this.http.post('api/mf-authenticate/code',credentials, {headers, observe: 'body'})
    }

    submitOPTCode(credentials) {
        const body = new HttpParams().append('userName', 'admin');
        const headers = new HttpHeaders().append('Accept', 'application/json');
        return this.http.post('api/mf-authenticate', credentials, {headers, observe: 'body'});
    }

    logout(): Observable<void> {
        return this.http.post('api/logout', {observe: 'body'})
          .pipe(map(() => {}));
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
