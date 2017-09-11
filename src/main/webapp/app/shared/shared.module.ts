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

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule
    ],
    declarations: [
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        AttributeMapperComponent
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
        DatePipe
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]

})
export class ManagementPortalSharedModule {}
