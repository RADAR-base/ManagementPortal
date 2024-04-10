import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { map, switchMap } from "rxjs/operators";
import {SessionService} from "../session/session.service";
import {environment} from "../../../environments/environment";

@Injectable({ providedIn: 'root' })
export class AuthServerProvider {

    logoutUrl;

    constructor(
            private http: HttpClient,
            private sessionService: SessionService,
    ) {
        sessionService.logoutUrl$.subscribe(
            url => this.logoutUrl = url
        )
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

    logout() {
        window.location.href = this.logoutUrl + "&return_to=" + environment.BASE_URL;
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
