import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Component({
    selector: 'jhi-queries',
    templateUrl: './queries.component.html',
})
export class QueriesComponent implements OnInit, OnDestroy {
    constructor(private httpclient: HttpClient) {}
    public queryList: any;

    private baseUrl = 'api/query-builder';

    getQueryList() {
        this.httpclient.get(this.baseUrl + '/queries').subscribe((result) => {
            this.queryList = result;
        });
    }

    ngOnInit() {
        this.getQueryList();
    }

    deleteQuery(id) {
        let httpParams = new HttpParams().set('id', id);

        let options = { body: { id: id } };

        // this.httpclient.request('delete', this.baseUrl + '/deleteQueryById', {
        //     body: { id: id },
        // });

        this.httpclient
            .delete(this.baseUrl + `/deleteQueryById/${id}`)
            .subscribe((result) => {
                console.log(result);
            });
        this.getQueryList();
    }

    ngOnDestroy() {}
}
