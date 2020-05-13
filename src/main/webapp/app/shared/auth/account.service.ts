import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class AccountService {
    constructor(private http: HttpClient) {
    }

    get(): Observable<any> {
        return this.http.get('api/account');
    }

    save(account: any): Observable<any> {
        return this.http.post('api/account', account);
    }
}
