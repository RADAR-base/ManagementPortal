import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../shared';

import {
    adminState,
    AuditsComponent,
    JhiDocsComponent,
    JhiHealthCheckComponent,
    JhiHealthModalComponent,
    JhiMetricsMonitoringComponent,
    LogsComponent,
    UserDeleteDialogComponent,
    UserDialogComponent,
    UserMgmtComponent,
    UserMgmtDeleteDialogComponent,
    UserMgmtDetailComponent,
    UserMgmtDialogComponent,
    UserSendActivationLinkComponent,
    UserSendActivationLinkDialogComponent,
} from './';
import {RoleComponent} from './user-management/role.component';
import {JvmMemoryComponent} from "./metrics/blocks/jvm-memory/jvm-memory.component";
import {JvmThreadsComponent} from "./metrics/blocks/jvm-threads/jvm-threads.component";
import {MetricsCacheComponent} from "./metrics/blocks/metrics-cache/metrics-cache.component";
import {MetricsDatasourceComponent} from "./metrics/blocks/metrics-datasource/metrics-datasource.component";
import {
    MetricsEndpointsRequestsComponent
} from "./metrics/blocks/metrics-endpoints-requests/metrics-endpoints-requests.component";
import {
    MetricsGarbageCollectorComponent
} from "./metrics/blocks/metrics-garbagecollector/metrics-garbagecollector.component";
import {MetricsModalThreadsComponent} from "./metrics/blocks/metrics-modal-threads/metrics-modal-threads.component";
import {MetricsRequestComponent} from "./metrics/blocks/metrics-request/metrics-request.component";
import {MetricsSystemComponent} from "./metrics/blocks/metrics-system/metrics-system.component";
import {ManagementPortalSharedSubjectModule} from '../shared/subject/subject.module';

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(adminState, {useHash: true}),
        ManagementPortalSharedSubjectModule,
    ],
    declarations: [
        AuditsComponent,
        UserMgmtComponent,
        UserDialogComponent,
        UserDeleteDialogComponent,
        UserMgmtDetailComponent,
        UserMgmtDialogComponent,
        UserMgmtDeleteDialogComponent,
        LogsComponent,
        JhiMetricsMonitoringComponent,
        JvmMemoryComponent,
        JvmThreadsComponent,
        MetricsCacheComponent,
        MetricsDatasourceComponent,
        MetricsEndpointsRequestsComponent,
        MetricsGarbageCollectorComponent,
        MetricsModalThreadsComponent,
        MetricsRequestComponent,
        MetricsSystemComponent,
        JhiHealthCheckComponent,
        JhiHealthModalComponent,
        JhiDocsComponent,
        JhiMetricsMonitoringComponent,
        RoleComponent,
        UserSendActivationLinkComponent,
        UserSendActivationLinkDialogComponent,
    ],
    entryComponents: [
        UserMgmtDialogComponent,
        UserMgmtDeleteDialogComponent,
        UserSendActivationLinkDialogComponent,
        JhiHealthModalComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalAdminModule {
}
