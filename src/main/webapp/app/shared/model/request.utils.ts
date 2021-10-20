import { HttpParams } from '@angular/common/http';

const addRequestOptions = (options: HttpParams, key: string, value?: any): HttpParams => {
    if (typeof value === 'undefined' || value === null || value === '') {
        // do nothing
    } else if (Array.isArray(value)) {
        value.forEach((v) => {
            options = addRequestOptions(options, key, v);
        });
    } else if (typeof value === 'object') {
        Object.keys(value).forEach((k) => {
            options = addRequestOptions(options, key + '.' + k, value[k]);
        })
    } else {
        options = options.append(key, value);
    }
    return options;
}

export const createRequestOption = (req?: any): HttpParams => {
    let options: HttpParams = new HttpParams();
    if (req) {
        Object.keys(req).forEach((key) => {
            options = addRequestOptions(options, key, req[key]);
        });
    }
    console.log(options);
    return options;
};
