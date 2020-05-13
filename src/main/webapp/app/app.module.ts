import {Injector, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {Ng2Webstorage} from 'ngx-webstorage';
import { ManagementPortalAccountModule } from './account/account.module';
import { ManagementPortalAdminModule } from './admin/admin.module';
import { PaginationConfig } from './blocks/config/uib-pagination.config';
import { ManagementPortalEntityModule } from './entities/entity.module';
import { ManagementPortalHomeModule } from './home/home.module';

import {
    ActiveMenuDirective,
    ErrorComponent,
    FooterComponent,
    JhiMainComponent,
    LayoutRoutingModule,
    NavbarComponent,
    PageRibbonComponent,
    ProfileService,
} from './layouts';

import { ManagementPortalSharedModule, UserRouteAccessService } from './shared';
import './vendor.ts';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {AuthInterceptor} from './blocks/interceptor/auth.interceptor';
import {AuthExpiredInterceptor} from './blocks/interceptor/auth-expired.interceptor';
import {ErrorHandlerInterceptor} from './blocks/interceptor/errorhandler.interceptor';
import {EventManager} from 'ng-jhipster';
import {NotificationInterceptor} from './blocks/interceptor/notification.interceptor';

@NgModule({
    imports: [
        BrowserModule,
        LayoutRoutingModule,
        Ng2Webstorage.forRoot({prefix: 'jhi', separator: '-'}),
        ManagementPortalSharedModule,
        ManagementPortalHomeModule,
        ManagementPortalAdminModule,
        ManagementPortalAccountModule,
        ManagementPortalEntityModule,
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
        ProfileService,
        PaginationConfig,
        UserRouteAccessService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true,
            deps: [
                Injector,
            ]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthExpiredInterceptor,
            multi: true,
            deps: [
               Injector
            ]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorHandlerInterceptor,
            multi: true,
            deps: [
                EventManager
            ]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: NotificationInterceptor,
            multi: true,
            deps: [
                Injector
            ]
        },
    ],
    bootstrap: [JhiMainComponent],
})
export class ManagementPortalAppModule {
}
