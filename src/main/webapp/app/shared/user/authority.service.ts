import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class AuthorityService {
    private resourceUrl = 'api/authorities';

    constructor(private http: HttpClient) {
    }

    findAll(): Observable<string[]> {
        return this.http.get<string[]>(`${this.resourceUrl}/`);
    }

}
