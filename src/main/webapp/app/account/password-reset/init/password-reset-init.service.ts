import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class PasswordResetInit {

    constructor(private http: HttpClient) {
    }

    save(mail: string): Observable<any> {
        return this.http.post('api/account/reset_password/init', mail);
    }
}
