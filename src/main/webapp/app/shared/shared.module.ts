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
import { JhiAlertComponent } from './alert/alert.component';
import { JhiAlertErrorComponent } from './alert/alert-error.component';
import { JhiSortDirective } from './util/sort.directive';
import { JhiSortByDirective } from './util/sort-by.directive';
import { JhiOrderByDirective } from './util/sort-order-by.directive';
import { JhiSortOrderDirective } from "./util/sort-order.directive";

@NgModule({
    imports: [
        TranslateModule.forChild(),
        ManagementPortalSharedLibsModule,
        ManagementPortalSharedCommonModule,
        RouterModule,
    ],
    declarations: [
        JhiAlertComponent,
        JhiAlertErrorComponent,
        JhiLoginModalComponent,
        JhiSortDirective,
        JhiSortByDirective,
        JhiOrderByDirective,
        JhiSortOrderDirective,
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
        JhiAlertComponent,
        JhiAlertErrorComponent,
        JhiLoginModalComponent,
        JhiSortDirective,
        JhiSortByDirective,
        JhiOrderByDirective,
        JhiSortOrderDirective,
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
