import { Injector } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import {
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse
} from '@angular/common/http';

import { AlertService } from '../../shared/util/alert.service';

export class NotificationInterceptor implements HttpInterceptor {

    private alertService: AlertService;

    constructor(private injector: Injector) {
        setTimeout(() => this.alertService = injector.get(AlertService));
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).do((event: HttpEvent<any>) => {
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
                            const alertParam = alertParams;
                            this.alertService.success(alert, { param : alertParam }, null);
                        }
                    }
                }
            }
        }, (err: any) => {});
    }
}
