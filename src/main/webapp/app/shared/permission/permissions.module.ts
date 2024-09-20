import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

import {ManagementPortalSharedModule} from '../../shared';
import {PermissionComponent} from "./permission.component";

@NgModule({
    imports: [
        ManagementPortalSharedModule,
    ],
    declarations: [
        PermissionComponent
    ],
    entryComponents: [
        PermissionComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        PermissionComponent
    ],
})
export class ManagementPortalSharedPermissionModule {
}
