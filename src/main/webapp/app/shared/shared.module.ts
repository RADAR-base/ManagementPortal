import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';

import { CookieService } from 'angular2-cookie/services/cookies.service';
import {
    ManagementPortalSharedLibsModule,
    ManagementPortalSharedCommonModule,
    CSRFService,
    AuthService,
    AuthServerProvider,
    AccountService,
    UserService,
    StateStorageService,
    LoginService,
    LoginModalService,
    Principal,
    HasAnyAuthorityDirective,
    JhiLoginModalComponent
} from './';
import {AuthorityService} from "./user/authority.service";
import {AttributeMapperComponent} from "./attribute-mapper/attribute-mapper.component";
import {CommonUserMgmtComponent} from "./user/common-user-management.component";
import {RouterModule} from "@angular/router";

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule,
        RouterModule
    ],
    declarations: [
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        AttributeMapperComponent,
        CommonUserMgmtComponent
    ],
    providers: [
        CookieService,
        LoginService,
        LoginModalService,
        AccountService,
        StateStorageService,
        Principal,
        CSRFService,
        AuthServerProvider,
        AuthService,
        UserService,
        AuthorityService,
        DatePipe
    ],
    entryComponents: [JhiLoginModalComponent],
    exports: [
        ManagementPortalSharedCommonModule,
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        AttributeMapperComponent,
        CommonUserMgmtComponent,
        DatePipe
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]

})
export class ManagementPortalSharedModule {}
