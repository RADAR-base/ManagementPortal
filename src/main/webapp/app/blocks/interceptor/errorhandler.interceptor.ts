import { Injectable, Injector } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest
} from '@angular/common/http';

import { EventManager } from '../../shared/util/event-manager.service';
import { AuthService } from "../../shared";

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {

    constructor(private eventManager: EventManager, private injector: Injector) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
          tap(
            () => {},
            (err: HttpErrorResponse) => {
              if (err.status === 401) {
                  this.injector.get(AuthService).resetAuthentication();
                  return;
              }
              this.eventManager.broadcast({
                  name: 'managementPortalApp.httpError',
                  content: err
              });
          }),
        );
    }
}
