import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {User} from './user.model';
import {createRequestOption} from '../model/request.utils';

@Injectable({providedIn: 'root'})
export class UserService {
    private resourceUrl = 'api/users';

    constructor(private http: HttpClient) {
    }

    create(user: User): Observable<any> {
        return this.http.post(this.resourceUrl, user);
    }

    update(user: User): Observable<any> {
        return this.http.put(this.resourceUrl, user);
    }

    find(login: string): Observable<User> {
        return this.http.get<User>(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    query(req?: any): Observable<HttpResponse<User[]>> {
        const params = createRequestOption(req);
        return this.http.get<User[]>(this.resourceUrl, {params, observe: 'response'});
    }

    delete(login: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    sendActivation(login: string): Observable<any> {
        return this.http.post('api/account/reset-activation/init', login);
    }

    findByProjectAndAuthority(req: any): Observable<User[]> {
        const params = createRequestOption(req);
        return this.http.get<User[]>(this.resourceUrl, {params});
    }
}
