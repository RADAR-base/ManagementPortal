import { Injectable, Injector } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
    HttpErrorResponse,
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
              if (err.status === 401 && request.url === 'api/account') {
                  // If the account endpoint says 401, the user is no longer authenticated:
                  // clear authentication and redirect to login/access denied.
                  this.injector.get(AuthService).resetAuthentication(true);
                  return;
              }
              if (err.status === 409 && request.method === 'DELETE') {
                  // don't report error about this.
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
