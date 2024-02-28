import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class Password {

    constructor(private http: HttpClient) {
    }

    save(newPassword: string): Observable<any> {
        return this.http.post('api/account/change_password', newPassword);
    }

    measureStrength(p: string): number {
        const lowerCase = /[a-z]+/.test(p);
        const upperCase = /[A-Z]+/.test(p);
        const numbers = /[0-9]+/.test(p);
        const symbols = /[^a-zA-Z0-9]/.test(p);

        const flags = [lowerCase, upperCase, numbers, symbols];
        const passedMatches = flags.filter((isMatchedFlag) => isMatchedFlag).length;

        let force = 2 * p.length + passedMatches * 8;

        force = (p.length < 12) ? Math.min(force, 20) : force;

        return force;
    }
}
