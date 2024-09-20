import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HttpClientModule} from '@angular/common/http';

@NgModule({
    imports: [
        NgbModule,
    ],
    exports: [
        FormsModule,
        HttpClientModule,
        CommonModule,
        NgbModule,
    ],
})
export class ManagementPortalSharedLibsModule {
}
