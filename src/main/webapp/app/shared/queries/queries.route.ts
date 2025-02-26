import { Routes } from '@angular/router';

import { QueriesComponent } from './queries.component';
import { AddQueryComponent } from './addQuery.component';

export const queriesRoute: Routes = [
    {
        path: 'queries',
        component: QueriesComponent,
        data: {
            authorities: [],
            pageTitle: 'global.menu.queries',
        },
    },
    {
        path: 'add-query',
        component: AddQueryComponent,
        data: {
            authorities: [],
            pageTitle: 'global.menu.queries',
        },
    },
];
