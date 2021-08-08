import { NgModule, Sanitizer } from '@angular/core';
import { Title } from '@angular/platform-browser';
import {
    FindLanguageFromKeyPipe,
    JhiAlertComponent,
    JhiAlertErrorComponent,
    JhiLanguageHelper,
    ManagementPortalSharedLibsModule,
} from './';
import { AlertService } from './util/alert.service';

export function alertServiceProvider(sanitizer: Sanitizer) {
    return new AlertService(sanitizer);
}

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
    ],
    declarations: [
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent,
    ],
    providers: [
        JhiLanguageHelper,
        {
            provide: AlertService,
            useFactory: alertServiceProvider,
            deps: [Sanitizer],
        },
        Title,
    ],
    exports: [
        ManagementPortalSharedLibsModule,
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent,
    ],
})
export class ManagementPortalSharedCommonModule {
}
