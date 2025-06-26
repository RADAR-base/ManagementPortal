import { Component, OnDestroy, OnInit } from '@angular/core';
import { QueriesService } from './queries.service';

@Component({
    selector: 'jhi-queries',
    templateUrl: './queryGroupList.component.html',
    styleUrls: ['./queryGroupList.component.scss'],
})
export class QueryGroupListComponent implements OnInit, OnDestroy {
    constructor(private queriesService: QueriesService) {}
    public queryGroupList: any;

    getQueryGroupList() {
        this.queriesService.getQueryGroupList().subscribe((result) => {
            this.queryGroupList = result;
        });
    }

    ngOnInit() {
        this.getQueryGroupList();
    }

    deleteQueryGroup(id) {
        this.queriesService.deleteQueryGroup(id).subscribe(() => {
            this.getQueryGroupList();
        });
    }

    ngOnDestroy() {}
}
