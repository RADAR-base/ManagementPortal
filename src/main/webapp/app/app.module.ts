import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { Ng2Webstorage } from 'ng2-webstorage';
import { ManagementPortalAccountModule } from './account/account.module';
import { ManagementPortalAdminModule } from './admin/admin.module';
import { PaginationConfig } from './blocks/config/uib-pagination.config';
import { customHttpProvider } from './blocks/interceptor/http.provider';
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
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService,
    ],
    bootstrap: [JhiMainComponent],
})
export class ManagementPortalAppModule {
}
