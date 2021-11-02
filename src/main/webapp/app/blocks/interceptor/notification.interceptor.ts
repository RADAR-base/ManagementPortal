import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse
} from '@angular/common/http';

import { AlertService } from '../../shared/util/alert.service';

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {

    constructor(private alertService: AlertService) {
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(tap((event) => {
            if (event instanceof HttpResponse) {
                const arr = event.headers.keys();
                let alert = null;
                let alertParams = null;
                arr.forEach((entry) => {
                    if (entry.toLocaleLowerCase().endsWith('app-alert')) {
                        alert = event.headers.get(entry);
                    } else if (entry.toLocaleLowerCase().endsWith('app-params')) {
                        alertParams = event.headers.get(entry);
                    }
                });
                if (alert) {
                    if (typeof alert === 'string') {
                        if (this.alertService) {
                            this.alertService.success(alert, { param : alertParams }, null);
                        }
                    }
                }
            }
        }));
    }
}
