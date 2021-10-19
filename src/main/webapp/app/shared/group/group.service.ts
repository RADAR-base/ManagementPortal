import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Group } from "./group.model";

@Injectable({ providedIn: 'root' })
export class GroupService {
    constructor(private http: HttpClient) {
    }

    private resourceUrl(projectName: string, groupName?: string): string {
        let url = 'api/projects/' + encodeURIComponent(projectName) + '/groups';
        if (groupName) {
            url += '/' + encodeURIComponent(groupName);
        }
        return url;
    }

    list(projectName: string): Observable<Group[]> {
        return this.http.get<Group[]>(this.resourceUrl(projectName));
    }

    create(projectName: string, group: Group): Observable<Group> {
        const copy: Group = Object.assign({}, group);
        return this.http.post<Group>(this.resourceUrl(projectName), copy);
    }

    delete(projectName: string, groupName: string): Observable<any> {
        return this.http.delete(this.resourceUrl(projectName, groupName));
    }
}
