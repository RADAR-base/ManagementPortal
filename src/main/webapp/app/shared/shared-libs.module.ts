import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgJhipsterModule } from 'ng-jhipster';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
    imports: [
        NgbModule.forRoot(),
        NgJhipsterModule.forRoot({
            i18nEnabled: true,
            defaultI18nLang: 'en',
        }),
    ],
    exports: [
        FormsModule,
        HttpClientModule,
        CommonModule,
        NgbModule,
        NgJhipsterModule,
    ],
})
export class ManagementPortalSharedLibsModule {
}
