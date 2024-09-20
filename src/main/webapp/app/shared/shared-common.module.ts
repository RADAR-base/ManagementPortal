import {NgModule} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {FindLanguageFromKeyPipe, ManagementPortalSharedLibsModule,} from './';

@NgModule({
    imports: [
        ManagementPortalSharedLibsModule,
    ],
    declarations: [
        FindLanguageFromKeyPipe,
    ],
    providers: [
        Title,
    ],
    exports: [
        ManagementPortalSharedLibsModule,
        FindLanguageFromKeyPipe,
    ],
})
export class ManagementPortalSharedCommonModule {
}
