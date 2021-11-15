import { Component, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';

@Component({
    selector: 'jhi-alert-error',
    template: `
        <div class="alerts" role="alert">
            <div *ngFor="let alert of alerts"  [ngClass]="{\'alert.position\': true, \'toast\': alert.toast}">
                <ngb-alert type="{{alert.type}}" close="alert.close(alerts)">
                    <pre [innerHTML]="alert.msg | translate:alert.params"></pre>
                </ngb-alert>
            </div>
        </div>`,
})
export class JhiAlertErrorComponent implements OnDestroy {

    alerts: any[];
    cleanHttpErrorListener: Subscription;

    constructor(
        private alertService: AlertService,
        private eventManager: EventManager,
        translateService: TranslateService,
    ) {
        this.alerts = [];

        this.cleanHttpErrorListener = eventManager.subscribe('managementPortalApp.httpError', (response) => {
            let i;
            const httpErrorResponse = response.content;
            switch (httpErrorResponse.status) {
                    // connection refused, server not reachable
                case 0:
                    this.addErrorAlert('Server not reachable', 'error.server.not.reachable');
                    break;

                case 400:
                case 409:
                    const arr = httpErrorResponse.headers.keys();
                    let errorHeader = null;
                    let entityKey = null;
                    arr.forEach((entry) => {
                        if (entry.endsWith('app-error')) {
                            errorHeader = httpErrorResponse.headers.get(entry);
                        } else if (entry.endsWith('app-params')) {
                            entityKey = httpErrorResponse.headers.get(entry);
                        }
                    });
                    if (errorHeader) {
                        const entityName = translateService.instant('global.menu.entities.' + entityKey);
                        this.addErrorAlert(errorHeader, errorHeader, {entityName});
                    } else if (httpErrorResponse.error !== '' && httpErrorResponse.error.fieldErrors) {
                        const fieldErrors = httpErrorResponse.error.fieldErrors;
                        for (i = 0; i < fieldErrors.length; i++) {
                            const fieldError = fieldErrors[i];
                            // convert 'something[14].other[4].id' to 'something[].other[].id' so translations can be written to it
                            const convertedField = fieldError.field.replace(/\[\d*]/g, '[]');
                            const fieldName = translateService.instant('managementPortalApp.' +
                                    fieldError.objectName + '.' + convertedField);
                            this.addErrorAlert(
                                    'Field ' + fieldName + ' cannot be empty', 'error.' + fieldError.message, {fieldName});
                        }
                    } else if (httpErrorResponse.error !== '' && httpErrorResponse.error.message) {
                        this.addErrorAlert(httpErrorResponse.error.message, httpErrorResponse.error.message, httpErrorResponse.error.params);
                    } else {
                        this.addErrorAlert(httpErrorResponse.error);
                    }
                    break;

                case 404:
                    this.addErrorAlert('Not found', 'error.url.not.found');
                    break;

                default:
                    if (httpErrorResponse.error !== '' && httpErrorResponse.error.message) {
                        this.addErrorAlert(httpErrorResponse.error.message);
                    } else {
                        this.addErrorAlert(httpErrorResponse.error);
                    }
            }
        });
    }

    ngOnDestroy() {
        if (this.cleanHttpErrorListener !== undefined && this.cleanHttpErrorListener !== null) {
            this.eventManager.destroy(this.cleanHttpErrorListener);
            this.alerts = [];
        }
    }

    addErrorAlert(message, key?, data?) {
        key = key ? key : message;
        window.console.log(key, data)
        this.alerts.push(
                this.alertService.addAlert(
                        {
                            type: 'danger',
                            msg: key,
                            params: data,
                            timeout: 5000,
                            scoped: true,
                        },
                        this.alerts,
                ),
        );
    }
}
