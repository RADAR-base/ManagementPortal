import { DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CookieService } from 'ngx-cookie-service';
import {
    AuthServerProvider,
    AuthService,
    HasAnyAuthorityDirective,
    JhiLoginModalComponent,
    LoginModalService,
    LoginService,
    ManagementPortalSharedCommonModule,
    ManagementPortalSharedLibsModule,
    StateStorageService,
} from './';
import { DictionaryMapperComponent } from './dictionary-mapper/dictionary-mapper.component';
import { ShowMoreComponent } from './show-more/show-more.component';
import { AuthorityService } from './user/authority.service';
import { CommonUserMgmtComponent } from './user/common-user-management.component';
import { ResolvePagingParams } from './commons';
import { ProjectService } from './project/project.service';

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule,
        RouterModule,
    ],
    declarations: [
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
    ],
    providers: [
        CookieService,
        LoginService,
        LoginModalService,
        StateStorageService,
        AuthServerProvider,
        AuthService,
        ProjectService,
        AuthorityService,
        DatePipe,
        ResolvePagingParams,
    ],
    entryComponents: [JhiLoginModalComponent],
    exports: [
        ManagementPortalSharedCommonModule,
        JhiLoginModalComponent,
        HasAnyAuthorityDirective,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
        DatePipe,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class ManagementPortalSharedModule {
}
