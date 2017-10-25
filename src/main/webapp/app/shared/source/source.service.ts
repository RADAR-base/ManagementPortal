import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Source } from './source.model';
import {Project} from "../../entities/project/project.model";
@Injectable()
export class SourceService {

    private resourceUrl = 'api/sources';
    private projectResourceUrl = 'api/projects';

    constructor(private http: Http) { }

    create(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(name: string): Observable<Source> {
        return this.http.get(`${this.resourceUrl}/${name}`).map((res: Response) => {
            return res.json();
        });
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
        ;
    }

    delete(sourceName: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${sourceName}`);
    }
    private createRequestOption(req?: any): BaseRequestOptions {
        const options: BaseRequestOptions = new BaseRequestOptions();
        if (req) {
            const params: URLSearchParams = new URLSearchParams();
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            params.set('query', req.query);

            options.search = params;
        }
        return options;
    }

    findAllByProject(req?: any) :  Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        const options = {
            search: params
        };
        return this.http.get(`${this.projectResourceUrl}/${req.projectName}/sources`, options);
    }

    findAvailable(req?: any): Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        if (req) {
            params.set('assigned', req.assigned);
        }
        params.set('minimized', 'true');
        const options = {
            search: params
        };
        return this.http.get(`${this.projectResourceUrl}/${req.projectName}/sources` , options);
    }
}
