import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from "rxjs/operators";
import {Group} from "./group.model";
import {createRequestOption} from "../model/request.utils";

@Injectable({providedIn: 'root'})
export class GroupService {
    constructor(private http: HttpClient) {
    }

    list(projectName: string): Observable<Group[]> {
        return this.http.get<Group[]>(this.resourceUrl(projectName));
    }

    find(id: number, projectName: string): Observable<Group> {
        return this.list(projectName).pipe(
            map(groups => groups.filter(g => g.id == id)[0])
        );
    }

    create(projectName: string, group: Group): Observable<Group> {
        const copy: Group = Object.assign({}, group);
        return this.http.post<Group>(this.resourceUrl(projectName), copy);
    }

    delete(projectName: string, groupName: string, forceDelete?: boolean): Observable<any> {
        return this.http.delete(this.resourceUrl(projectName, groupName), {
            params: createRequestOption({unlinkSubjects: forceDelete}),
        });
    }

    addSubjectsToGroup(
        projectName: string, groupName: string,
        subjects: { login?: string, id?: number; }[]
    ) {
        let baseUrl = this.resourceUrl(projectName, groupName);
        let body = [{op: "add", value: subjects}];
        return this.http.patch(`${baseUrl}/subjects`, body);
    }

    protected resourceUrl(projectName: string, groupName?: string): string {
        let url = 'api/projects/' + encodeURIComponent(projectName) + '/groups';
        if (groupName) {
            url += '/' + encodeURIComponent(groupName);
        }
        return url;
    }
}
