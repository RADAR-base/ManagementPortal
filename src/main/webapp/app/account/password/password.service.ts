import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class Password {

    constructor(private http: HttpClient) {
    }

    save(newPassword: string): Observable<any> {
        return this.http.post('api/account/change_password', newPassword);
    }
}
