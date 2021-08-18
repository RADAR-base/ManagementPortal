import { NgModule } from '@angular/core';
import { Title } from '@angular/platform-browser';
import {
    FindLanguageFromKeyPipe,
    JhiAlertComponent,
    JhiAlertErrorComponent,
    ManagementPortalSharedLibsModule,
} from './';

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
    ],
    declarations: [
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent,
    ],
    providers: [
        Title,
    ],
    exports: [
        ManagementPortalSharedLibsModule,
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent,
    ],
})
export class ManagementPortalSharedCommonModule {
}
