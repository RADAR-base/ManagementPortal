import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Account } from "../user/account.model";

@Injectable({ providedIn: 'root' })
export class AccountService {
    constructor(private http: HttpClient) {
    }

    get(): Observable<Account> {
        return this.http.get<Account>('api/account');
    }

    save(account: Account): Observable<Account> {
        return this.http.post<Account>('api/account', account);
    }
}
