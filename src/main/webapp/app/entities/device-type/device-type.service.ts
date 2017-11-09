import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { DeviceType } from './device-type.model';
@Injectable()
export class DeviceTypeService {

    private resourceUrl = 'api/device-types';

    constructor(private http: Http) { }

    create(deviceType: DeviceType): Observable<DeviceType> {
        const copy: DeviceType = Object.assign({}, deviceType);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(deviceType: DeviceType): Observable<DeviceType> {
        const copy: DeviceType = Object.assign({}, deviceType);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(producer: string, model: string, version: string): Observable<DeviceType> {
        return this.http.get(`${this.resourceUrl}/${producer}/${model}/${version}`).map((res: Response) => {
            return res.json();
        });
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
        ;
    }

    delete(producer: string, model: string, version: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${producer}/${model}/${version}`);
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
