import { Injector } from '@angular/core';
import { Http, RequestOptions, XHRBackend } from '@angular/http';
import { EventManager, InterceptableHttp } from 'ng-jhipster';
import { AuthExpiredInterceptor } from './auth-expired.interceptor';

import { AuthInterceptor } from './auth.interceptor';
import { ErrorHandlerInterceptor } from './errorhandler.interceptor';
import { NotificationInterceptor } from './notification.interceptor';

export function interceptableFactory(
        backend: XHRBackend,
        defaultOptions: RequestOptions,
        injector: Injector,
        eventManager: EventManager,
) {
    return new InterceptableHttp(
            backend,
            defaultOptions,
            [
                new AuthInterceptor(injector),
                new AuthExpiredInterceptor(injector),
                // Other interceptors can be added here
                new ErrorHandlerInterceptor(eventManager),
                new NotificationInterceptor(injector),
            ],
    );
}

export function customHttpProvider() {
    return {
        provide: Http,
        useFactory: interceptableFactory,
        deps: [
            XHRBackend,
            RequestOptions,
            Injector,
            EventManager,
        ],
    };
}
