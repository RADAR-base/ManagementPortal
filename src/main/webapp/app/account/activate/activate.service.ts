import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class Activate {

    constructor(private http: HttpClient) {
    }

    get(key: string): Observable<any> {
        const params = {'key': key};
        return this.http.get('api/activate', {params});
    }
}
