import { HttpParams } from '@angular/common/http';

export const createRequestOption = (req?: RequestOptions): HttpParams => {
    let options: HttpParams = new HttpParams();
    if (req) {
        Object.keys(req).forEach((key) => {
            if (Array.isArray(req[key])) {
                const values: [string] = req[key] as [string];
                values.forEach(value => options.append(key, value));
            } else {
                const value = req[key] as string;
                options = options.set(key, value);
            }
        });
    }
    return options;
};

interface RequestOptions {
    [key: string]: string | [string];
}
