import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Role } from '../../admin/user-management/role.model';

@Injectable({ providedIn: 'root' })
export class RoleService {

    private resourceUrl = 'api/roles';

    constructor(private http: HttpClient) {
    }

    find(projectName: string, authorityName: string): Observable<Role> {
        return this.http.get<Role>(`${this.resourceUrl}/${encodeURIComponent(projectName)}/${encodeURIComponent(authorityName)}`);
    }
}
