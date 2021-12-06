import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Account } from "../user/account.model";

@Injectable({ providedIn: 'root' })
export class AccountService {
    constructor(private http: HttpClient) {
    }

    get(): Observable<Account> {
        return this.http.get<Account>('api/account');
    }

    save(account?: Account): Observable<Account> {
        if (!account) {
            return throwError('account not provided');
        }
        return this.http.post<Account>('api/account', account);
    }
}
