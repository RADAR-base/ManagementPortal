import { NgModule } from '@angular/core';
import {
    HttpClient,
    HttpClientModule,
    HTTP_INTERCEPTORS,
} from '@angular/common/http';
import { BrowserModule } from '@angular/platform-browser';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { ManagementPortalAccountModule } from './account/account.module';
import { ManagementPortalAdminModule } from './admin/admin.module';
import { PaginationConfig } from './blocks/config/uib-pagination.config';
import { ManagementPortalEntityModule } from './entities/entity.module';
import { ManagementPortalHomeModule } from './home';
import { ManagementPortalQueriesModule } from './shared';

import {
    ActiveMenuDirective,
    ErrorComponent,
    FooterComponent,
    JhiMainComponent,
    LayoutRoutingModule,
    NavbarComponent,
    PageRibbonComponent,
} from './layouts';

import { LANGUAGES, ManagementPortalSharedModule} from './shared';
import { ErrorHandlerInterceptor } from './blocks/interceptor/errorhandler.interceptor';
import { NotificationInterceptor } from './blocks/interceptor/notification.interceptor';
import { APP_BASE_HREF, PlatformLocation } from "@angular/common";

export function getBaseHref(platformLocation: PlatformLocation): string {
    return platformLocation.getBaseHrefFromDOM();
}

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        TranslateModule.forRoot({
            useDefaultLang: true,
            loader: {
                provide: TranslateLoader,
                useFactory: c => new TranslateHttpLoader(c, 'i18n/', `.json`),
                deps: [HttpClient],
            },
        }),
        LayoutRoutingModule,
        NgxWebstorageModule.forRoot({ prefix: 'jhi', separator: '-' }),
        ManagementPortalSharedModule,
        ManagementPortalHomeModule,
        ManagementPortalAdminModule,
        ManagementPortalAccountModule,
        ManagementPortalEntityModule,
        ManagementPortalQueriesModule
    ],
    declarations: [
        JhiMainComponent,
        NavbarComponent,
        ErrorComponent,
        PageRibbonComponent,
        ActiveMenuDirective,
        FooterComponent,
    ],
    providers: [
        PaginationConfig,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorHandlerInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: NotificationInterceptor,
            multi: true,
        },
        {
            provide: APP_BASE_HREF,
            useFactory: getBaseHref,
            deps: [PlatformLocation]
        }
    ],
    bootstrap: [JhiMainComponent],
})
export class ManagementPortalAppModule {
    constructor(translate: TranslateService) {
        let browserLang = translate.getBrowserLang();
        translate.use(LANGUAGES.includes(browserLang) ? browserLang: 'en');
    }
}
