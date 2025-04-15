import { Routes } from '@angular/router';

import { QueryGroupListComponent } from './queryGroupList.component';
import { AddQueryComponent } from './addQuery.component';

export const queriesRoute: Routes = [
    {
        path: 'querygroups',
        component: QueryGroupListComponent,
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
