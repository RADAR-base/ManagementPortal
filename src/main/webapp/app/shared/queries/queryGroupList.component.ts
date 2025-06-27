import { Component, OnDestroy, OnInit } from '@angular/core';
import { QueriesService } from './queries.service';
import { QueryGroup } from './queries.model';

@Component({
    selector: 'jhi-queries',
    templateUrl: './queryGroupList.component.html',
    styleUrls: ['./queryGroupList.component.scss'],
})
export class QueryGroupListComponent implements OnInit, OnDestroy {
    public queryGroupList: any[] = [];

    constructor(private queriesService: QueriesService) {}

    getQueryGroupList() {
        this.queriesService.getQueryGroupList().subscribe((result: QueryGroup[]) => {
            this.queryGroupList = result.filter(group => !group.isArchived);
        });
    }

    ngOnInit() {
        this.getQueryGroupList();
    }

    archiveQueryGroup(id: number) {
        if (confirm('Are you sure you want to archive this query group?')) {
            this.queriesService.canArchiveQueryGroup(id).subscribe((canArchive: boolean) => {
                if (!canArchive) {
                    alert('This query group cannot be archived because it is currently assigned to a participant/s');
                    return;
                }

                this.queriesService.archiveQueryGroup(id).subscribe(() => {
                    this.getQueryGroupList();
                });
            });
        }
    }

    ngOnDestroy() {}
}
