import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { HttpClient, HttpResponse } from '@angular/common/http';

@Injectable()
export class JhiConfigurationService {

    constructor(private http: HttpClient) {
    }

    get(): Observable<any> {
        return this.http.get('management/configprops').map((res: HttpResponse<any>) => {
            const properties: any[] = [];

            const propertiesObject = res.body;

            for (const key in propertiesObject) {
                if (propertiesObject.hasOwnProperty(key)) {
                    properties.push(propertiesObject[key]);
                }
            }

            return properties.sort((propertyA, propertyB) => {
                return (propertyA.prefix === propertyB.prefix) ? 0 :
                        (propertyA.prefix < propertyB.prefix) ? -1 : 1;
            });
        });
    }

    getEnv(): Observable<any> {
        return this.http.get('management/env').map((res: HttpResponse<any>) => {
            const properties: any = {};

            const propertiesObject = res.body;

            for (const key in propertiesObject) {
                if (propertiesObject.hasOwnProperty(key)) {
                    const valsObject = propertiesObject[key];
                    const vals: any[] = [];

                    for (const valKey in valsObject) {
                        if (valsObject.hasOwnProperty(valKey)) {
                            vals.push({key: valKey, val: valsObject[valKey]});
                        }
                    }
                    properties[key] = vals;
                }
            }

            return properties;
        });
    }
}
