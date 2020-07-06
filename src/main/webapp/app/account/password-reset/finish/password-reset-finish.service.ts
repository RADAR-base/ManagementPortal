import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class PasswordResetFinish {

    constructor(private http: HttpClient) {
    }

    save(keyAndPassword: any): Observable<any> {
        return this.http.post('api/account/reset_password/finish', keyAndPassword);
    }
}
