import {Routes} from '@angular/router';

import {activateRoute, passwordResetFinishRoute, passwordResetInitRoute, passwordRoute, settingsRoute,} from './';

const ACCOUNT_ROUTES = [
    activateRoute,
    passwordRoute,
    passwordResetFinishRoute,
    passwordResetInitRoute,
    settingsRoute,
];

export const accountState: Routes = [{
    path: '',
    children: ACCOUNT_ROUTES,
}];
