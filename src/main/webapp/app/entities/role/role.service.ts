import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Role } from '../../admin/user-management/role.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable()
export class RoleService {

    private resourceUrl = 'api/roles';

    constructor(private http: HttpClient) {
    }

    create(role: Role): Observable<Role> {
        const copy: Role = Object.assign({}, role);
        return this.http.post(this.resourceUrl, copy) as Observable<Role>;
    }

    update(role: Role): Observable<Role> {
        const copy: Role = Object.assign({}, role);
        return this.http.put(this.resourceUrl, copy) as Observable<Role>;
    }

    find(projectName: string, authorityName: string): Observable<Role> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(projectName)}/${encodeURIComponent(authorityName)}`) as Observable<Role>;
    }

    query(req?: any): Observable<HttpResponse<any>> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params: options, observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    delete(projectName: string, authorityName: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(projectName)}/${encodeURIComponent(authorityName)}`) as Observable<HttpResponse<any>>;
    }

}
