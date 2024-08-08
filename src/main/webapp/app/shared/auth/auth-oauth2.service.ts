import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { map } from 'rxjs/operators';
import { SessionService } from '../session/session.service';

@Injectable({ providedIn: 'root' })
export class AuthServerProvider {
    logoutUrl;

    constructor(
        private http: HttpClient,
        private sessionService: SessionService
    ) {
        sessionService.logoutUrl$.subscribe((url) => (this.logoutUrl = url));
    }

    login(accessToken: string): Observable<any> {
        const authHeaders = new HttpHeaders().append(
            'Authorization',
            'Bearer ' + accessToken,
        );
        return this.http.post('api/login', null, {
            headers: authHeaders,
            observe: 'body',
            withCredentials: true,
        })
    }

    logout() {
        return this.http
            .post('api/logout', { observe: 'body' })
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
