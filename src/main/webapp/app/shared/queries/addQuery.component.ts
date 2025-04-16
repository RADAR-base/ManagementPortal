import { Component, ViewChild } from '@angular/core';

import {
    QueryBuilderClassNames,
    QueryBuilderConfig,
} from '@pri17/ngx-angular-query-builder';
import { FormBuilder, FormControl, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { QueryDTO, QueryNode, QueryString } from './queries.model';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { QueryGroup } from './query.model';

@Component({
    selector: 'jhi-queries',
    templateUrl: './addQuery.component.html',
    styleUrls: ['../../../content/scss/queries.scss'],
})
export class AddQueryComponent {
    @ViewChild('queryForm', { static: true })
    queryBuilderFormGroup: NgForm;

    public queryCtrl: FormControl;

    public queryGrouName: string;

    public queryGroupDesc: string;

    public queryGroupId: any | null;

    private baseUrl = 'api/query-builder';

    public bootstrapClassNames: QueryBuilderClassNames = {
        removeIcon: 'fa fa-minus',
        addIcon: 'fa fa-plus',
        arrowIcon: 'fa fa-chevron-right px-2',
        button: 'btn',
        buttonGroup: 'btn-group',
        rightAlign: 'order-12 ml-auto',
        switchRow: 'd-flex px-2',
        switchGroup: 'd-flex align-items-center',
        switchRadio: 'custom-control-input',
        switchLabel: 'custom-control-label',
        switchControl: 'custom-control custom-radio custom-control-inline',
        row: 'row p-2 m-1',
        rule: 'border',
        ruleSet: 'border',
        invalidRuleSet: 'alert alert-danger',
        emptyWarning: 'text-danger mx-auto',
        operatorControl: 'form-control',
        operatorControlSize: 'col-auto pr-0',
        fieldControl: 'form-control',
        fieldControlSize: 'col-auto pr-0',
        entityControl: 'form-control',
        entityControlSize: 'col-auto pr-0',
        inputControl: 'form-control',
        inputControlSize: 'col-auto',
    };

    public query: QueryString | any = {
        condition: 'and',
        rules: [{ field: 'heart_rate', operator: '<=' }],
    };

    public config: QueryBuilderConfig = {
        fields: {
            heart_rate: { name: 'Heart Rate', type: 'number' },
            sleep_length: { name: 'Sleep', type: 'number' },
            wake_up_time: { name: 'Wake Up Time', type: 'number' },
        },
    };

    public currentConfig: QueryBuilderConfig;
    public allowRuleset: boolean = true;
    public allowCollapse: boolean;
    public persistValueOnFieldChange: boolean = false;

    private canGoBack: boolean = false;

    constructor(
        private formBuilder: FormBuilder,
        private http: HttpClient,
        private router: Router,
        private readonly location: Location,
              private route: ActivatedRoute,
    ) {
        this.queryCtrl = this.formBuilder.control(this.query);
        this.currentConfig = this.config;
        this.canGoBack =
            !!this.router.getCurrentNavigation()?.previousNavigation;
    }

    ngOnInit() {
        this.route.params.subscribe((params) => {
            console.log("route params", params['query-id'])
            let queryId = params["query-id"];
            this.queryGroupId = queryId;
            if (queryId) {

                this.http
                .get(this.baseUrl + '/querygroups/' + queryId)
                    .subscribe((response: any) => {
                        this.query = response

                        this.queryGrouName = response.queryGroupName;
                        this.queryGroupDesc = response.queryGroupDescription;
                });
            }
        });
    }

    goBack(): void {
        if (this.canGoBack) {
            this.location.back();
        }
    }
    changeDisabled(event: Event) {
        (<HTMLInputElement>event.target).checked
            ? this.queryCtrl.disable()
            : this.queryCtrl.enable();
    }

    private _counter = 0;
    formRuleWeakMap = new WeakMap();

    getUniqueName(prefix: string, rule: any) {
        if (!this.formRuleWeakMap.has(rule)) {
            this.formRuleWeakMap.set(rule, `${prefix}-${++this._counter}`);
        }

        return this.formRuleWeakMap.get(rule);
    }

    convertComparisonOperator(value?: string) {
        switch (value) {
            case '>=':
                return 'GREATER_THAN_OR_EQUALS';
            case '=':
                return 'EQUALS';
            case '!=':
                return 'NOT_EQUALS';
            case '>':
                return 'GREATER_THAN';
            case '<':
                return 'LESS_THAN';
            case '<=':
                return 'LESS_THAN_OR_EQUALS';
            default:
                return null;
        }
    }

    convertTimeFrame(value: number) {
        switch (value) {
            case 180:
                return 'PAST_6_MONTH';
            case 30:
                return 'PAST_MONTH';
            case 7:
                return 'LAST_7_DAYS';
            case 365:
                return 'PAST_YEAR';
            default:
                return null;
        }
    }

    convertQuery(query: QueryString): QueryNode {
        if (query.rules && query.rules.length > 0) {
            return {
                logic_operator: query.condition?.toUpperCase() || 'AND',
                children: query.rules.map((rule) => this.convertQuery(rule)),
            };
        } else {
            const queryDTO  : QueryDTO = {
                metric: query.field?.toUpperCase() || '',
                operator: this.convertComparisonOperator(query.operator),
                time_frame: this.convertTimeFrame(query.timeFame),
                value: query.value,
            };

            return {
                query: queryDTO,
            };
        }
    }

    saveQueryGroupToDB() {
        const query_group  : QueryGroup = {
            name: this.queryGrouName,
            description: this.queryGroupDesc,
        };

        if (this.queryGroupId) {
            this.updateQueryGroup(query_group);
        } else {
            this.saveNewQueryGroup(query_group);
        }
    }

    saveNewQueryGroup(queryGroup: QueryGroup) {
        this.http
            .post(this.baseUrl + '/query-group', queryGroup)
            .subscribe((id) => {
                const query_logic = {
                    queryGroupId: id,
                    ...this.convertQuery(this.query),
                };
                this.queryGroupId = id
                this.saveIndividualQueries(query_logic)
            });
    }
    updateQueryGroup(queryGroup: QueryGroup) {
        this.http
            .put(this.baseUrl + '/querygroups/' + this.queryGroupId , queryGroup)
            .subscribe((id) => {
                const query_logic = {
                    queryGroupId: id,
                    ...this.convertQuery(this.query),
                };

                this.updateIndividualQueries(query_logic)
            });
    }
    saveIndividualQueries(query_logic: QueryNode) {
        this.http
        .post(this.baseUrl + '/query-logic', query_logic)
        .subscribe((res) => {
            this.goBack();
        });
    }

    updateIndividualQueries(query_logic: QueryNode) {
        this.http
        .put(this.baseUrl + '/querylogic', query_logic)
        .subscribe((res) => {
            this.goBack();
        });
    }
}
