import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { queriesRoute } from './queries.route';
import { QueriesComponent } from './queries.component';
import { RouterModule } from '@angular/router';
import { ManagementPortalSharedModule } from '../shared.module';

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot([queriesRoute], { useHash: true }),
    ],
    declarations: [QueriesComponent],
    entryComponents: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalQueriesModule {}
