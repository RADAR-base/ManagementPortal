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

    measureStrength(p: string): number {
        let force = 0;
        const regex = /[$-/:-?{-~!"^_`\[\]]/g; // "
        const letters = /[a-zA-Z]+/.test(p);
        const numbers = /[0-9]+/.test(p);
        const symbols = regex.test(p);

        const flags = [letters, numbers, symbols];
        const passedMatches = flags.filter((isMatchedFlag: boolean) => {
            return isMatchedFlag === true;
        }).length;

        force += 2 * p.length + ((p.length >= 10) ? 1 : 0);
        force += passedMatches * 10;

        // penality (short password)
        force = (p.length <= 8) ? Math.min(force, 20) : force;

        // penality (poor variety of characters)
        force = (passedMatches === 1) ? Math.min(force, 10) : force;
        force = (passedMatches === 2) ? Math.min(force, 30) : force;

        return force;
    }
}
