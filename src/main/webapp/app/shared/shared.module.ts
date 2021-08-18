import { DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { CookieService } from 'ngx-cookie-service';
import {
    HasAnyAuthorityDirective,
    JhiLoginModalComponent,
    ManagementPortalSharedCommonModule,
    ManagementPortalSharedLibsModule,
} from './';
import { DictionaryMapperComponent } from './dictionary-mapper/dictionary-mapper.component';
import { ShowMoreComponent } from './show-more/show-more.component';
import { CommonUserMgmtComponent } from './user/common-user-management.component';
import { JhiSortDirective } from './util/sort.directive';
import { JhiSortByDirective } from './util/sort-by.directive';

@NgModule({
    imports: [
        TranslateModule.forChild(),
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule,
        RouterModule,
    ],
    declarations: [
        JhiLoginModalComponent,
        JhiSortDirective,
        JhiSortByDirective,
        HasAnyAuthorityDirective,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
    ],
    providers: [
        CookieService,
        DatePipe,
    ],
    exports: [
        ManagementPortalSharedCommonModule,
        JhiLoginModalComponent,
        JhiSortDirective,
        JhiSortByDirective,
        HasAnyAuthorityDirective,
        DictionaryMapperComponent,
        CommonUserMgmtComponent,
        ShowMoreComponent,
        DatePipe,
        TranslateModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class ManagementPortalSharedModule {
}
