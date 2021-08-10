import { DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CookieService } from 'ngx-cookie-service';
import {
    AuthServerProvider,
    AuthService,
    HasAnyAuthorityDirective,
    JhiLoginModalComponent,
    ManagementPortalSharedCommonModule,
    ManagementPortalSharedLibsModule,
} from './';
import { DictionaryMapperComponent } from './dictionary-mapper/dictionary-mapper.component';
import { ShowMoreComponent } from './show-more/show-more.component';
import { CommonUserMgmtComponent } from './user/common-user-management.component';

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
        AuthServerProvider,
        AuthService,
        DatePipe,
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
