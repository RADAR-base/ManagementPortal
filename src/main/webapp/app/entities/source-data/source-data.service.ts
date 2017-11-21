import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { SourceData } from './source-data.model';
@Injectable()
export class SourceDataService {

    private resourceUrl = 'api/source-data';

    constructor(private http: Http) { }

    create(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(sourceDataName: string): Observable<SourceData> {
        return this.http.get(`${this.resourceUrl}/${sourceDataName}`).map((res: Response) => {
            return res.json();
        });
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
        ;
    }

    delete(sourceDataName: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${sourceDataName}`);
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
}
