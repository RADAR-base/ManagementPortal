import { Component, ViewChild } from '@angular/core';

import {
    QueryBuilderClassNames,
    QueryBuilderConfig,
} from '@pri17/ngx-angular-query-builder';
import { FormBuilder, FormControl, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

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

    public query = {
        logic_operator: 'and',
        rules: [{ metric: 'heart_rate', operator: '<=' }],
    };

    public config: QueryBuilderConfig = {
        fields: {
            heart_rate: { name: 'Heart Rate', type: 'number' },
            sleep: { name: 'Sleep', type: 'number' },
            wake_up_time: { name: 'Wake Up Time', type: 'number' },
        },
    };

    public currentConfig: QueryBuilderConfig;
    public allowRuleset: boolean = true;
    public allowCollapse: boolean;
    public persistValueOnFieldChange: boolean = false;

    constructor(private formBuilder: FormBuilder, private http: HttpClient) {
        this.queryCtrl = this.formBuilder.control(this.query);
        this.currentConfig = this.config;
    }

    ngAfterViewInit() {
        //console.log(this.queryBuilderFormGroup)
        setTimeout(() => this.queryBuilderFormGroup.form.markAllAsTouched(), 0);
        //this.queryBuilderFormGroup.form.markAllAsTouched();
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

    convertComparisonOperator(value: string) {
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
        }
    }

    saveQueryGroupToDB() {
        let query_group = {
            name: this.queryGrouName,
            description: this.queryGroupDesc,
        };
        let query_id;

        let converted_query = [];
        this.query.rules.forEach((element) => {
            let newele = {
                metric: element.metric.toUpperCase(),
                operator: this.convertComparisonOperator(element.operator),
                time_frame: this.convertTimeFrame(element['time_frame']),
                value: element['value'],
            };
            converted_query.push({ query: newele });
        });

        this.http
            .post(this.baseUrl + '/query-group', query_group)
            .subscribe((id) => {
                query_id = id;
                let query_logic = {
                    queryGroupId: query_id,
                    logic_operator: this.query.logic_operator.toUpperCase(),
                    children: [...converted_query],
                };

                this.http
                    .post(this.baseUrl + '/query-logic', query_logic)
                    .subscribe((res) => {
                        console.log(res);
                    });
            });
    }
}
