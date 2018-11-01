import { DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CookieService } from 'angular2-cookie/services/cookies.service';
import {
    AccountService,
    AuthServerProvider,
    AuthService,
    CSRFService,
    HasAnyAuthorityDirective,
    JhiLoginModalComponent,
    LoginModalService,
    LoginService,
    ManagementPortalSharedCommonModule,
    ManagementPortalSharedLibsModule,
    Principal,
    StateStorageService,
    UserService,
} from './';
import { AttributeMapperComponent } from './attribute-mapper/attribute-mapper.component';
import { DictionaryMapperComponent } from './dictionary-mapper/dictionary-mapper.component';
import { ShowMoreComponent } from './show-more/show-more.component';
import { AuthorityService } from './user/authority.service';
import { CommonUserMgmtComponent } from './user/common-user-management.component';
import { ResolvePagingParams } from './commons';

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule,
        RouterModule,
    ],
    declarations: [
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        AttributeMapperComponent,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
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
        DatePipe,
        ResolvePagingParams,
    ],
    entryComponents: [JhiLoginModalComponent],
    exports: [
        ManagementPortalSharedCommonModule,
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        AttributeMapperComponent,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
        DatePipe,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class ManagementPortalSharedModule {
}
