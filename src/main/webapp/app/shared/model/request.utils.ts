import {HttpParams} from '@angular/common/http';

const addRequestOptions = (options: HttpParams, key: string, value?: any): HttpParams => {
    if (typeof value === 'undefined' || value === null) { // || value === ''
        // do nothing
    } else if (Array.isArray(value)) {
        value.forEach((v) => {
            options = addRequestOptions(options, key, v);
        });
    } else if (typeof value === 'object') {
        Object.entries(value).forEach(([k, v]) => {
            options = addRequestOptions(options, key + '.' + k, v);
        })
    } else {
        options = options.append(key, value);
    }
    return options;
}

export const createRequestOption = (req?: any): HttpParams => {
    let options: HttpParams = new HttpParams();
    if (req) {
        Object.entries(req).forEach(([key, value]) => {
            options = addRequestOptions(options, key, value);
        });
    }
    return options;
};
