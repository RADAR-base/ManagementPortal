import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Component({
    selector: 'jhi-queries',
    templateUrl: './queryGroupList.component.html',
    styleUrls: ['./queryGroupList.component.scss'],
})
export class QueryGroupListComponent implements OnInit, OnDestroy {
    constructor(private httpclient: HttpClient) {}
    public queryGroupList: any;

    private baseUrl = 'api/query-builder';

    getQueryGroupList() {
        this.httpclient.get(this.baseUrl + '/querygroups').subscribe((result) => {
            this.queryGroupList = result;
        });
    }

    ngOnInit() {
        this.getQueryGroupList();
    }

    deleteQueryGroup(id) {
        this.httpclient
            .delete(this.baseUrl + `/querygroup/${id}`)
            .subscribe((result) => {
                this.getQueryGroupList();
            });
    }

    ngOnDestroy() {}
}
