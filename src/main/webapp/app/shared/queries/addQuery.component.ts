import { Component, ViewChild, AfterViewInit } from '@angular/core';

import {
    QueryBuilderClassNames,
    QueryBuilderConfig,
} from '@uom-digital-health-software/ngx-angular-query-builder';
import { FormBuilder, FormControl, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { QueryDTO, QueryNode, QueryString } from './queries.model';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { QueryGroup } from './query.model';
import { QueriesService } from './queries.service';
import { ContentComponent } from './content/content.component';

import { delusions, questionnaire } from './questionnaire';

const sliderOptions = Array.from({ length: 7 }, (_, i) => {
    const val = String(i + 1);
    return { name: val, value: val };
});

@Component({
    selector: 'jhi-queries',
    templateUrl: './addQuery.component.html',
    styleUrls: ['../../../content/scss/queries.scss'],
})
export class AddQueryComponent {


    @ViewChild(ContentComponent) contentComponent!: ContentComponent;

    queryBuilderFormGroup: NgForm;

    public queryCtrl: FormControl;

    public queryGrouName: string;

    public queryGroupDesc: string;

    public queryGroupId: number | any | null;

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
        operatorControl: 'query-builder-field',
        operatorControlSize: 'col-auto pr-0',
        fieldControl: 'query-builder-field',
        fieldControlSize: 'col-auto pr-0',
        entityControl: 'query-builder-field',
        entityControlSize: 'col-auto pr-0',
        inputControl: 'query-builder-field',
        inputControlSize: 'col-auto pr-0',
    };

    public query: QueryString | any = {
        condition: 'and',
        rules: [],
    };

    public config: QueryBuilderConfig = {
        entities: {
            passive_data: { name: "Passive data" },
            questionnaire: { name: "Questionnaire Slider" },
            questionnaire_radio: { name: "Questionnaire Multichoice" },
            questionnaire_group: { name: "Questionnaire Group" },
            delusions: { name: "Delusions" }
        },
        fields: {
            heart_rate: { name: 'Heart Rate', type: 'number', entity: "passive_data" },
            sleep_length: { name: 'Sleep', type: 'number', entity: "passive_data" },
            hrv: { name: 'HRV', type: 'number', entity: "passive_data" },
        },
    };

    public currentConfig: QueryBuilderConfig;
    public allowRuleset: boolean = true;
    public allowCollapse: boolean;
    public persistValueOnFieldChange: boolean = true;



    private canGoBack: boolean = false;

    constructor(
        private queryService: QueriesService,
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

        // process the config

        this.addQuestionnaireItemsToQueryBuilder();

        this.addDelusionsToQueryBuilder();
    }

    private addQuestionnaireItemsToQueryBuilder() {
        // histogram to include only
        let histogramQuestionsToInclude = ["whereabouts_1", "sleep_5", "social_1"];

        for (const question of questionnaire) {
            const field = {
                name: `${question.field_label} ${question.field_sublabel ? question.field_sublabel : ""}`,
                type: "category",
                entity: "questionnaire",
                operators: null
            }
            if (question.field_type == "slider") {
                field["options"] = sliderOptions
            } else if(histogramQuestionsToInclude.includes(question.field_name)) {
                field.name = `${question.field_label}`
                field.entity = "questionnaire_radio"
                field.operators = ["IS"]
                let mappedOptions = question.select_choices_or_calculations.map((item) => {
                    return {
                        name: item.label,
                        value: item.code
                    }

                })
                field["options"] = mappedOptions
            }

            if (!this.config.fields[question.group_name]) {
                const group = {
                    name: `${question.group_name}`,
                    type: "category",
                    entity: "questionnaire_group",
                    options: sliderOptions
                }
                this.config.fields[question.group_name] = group
            }
            this.config.fields[question.field_name] = field
        }

    }

    private addDelusionsToQueryBuilder() {

        for (const delusion of delusions) {
            const field = {
                name: `${delusion.field_label} ${delusion.field_sublabel ? delusion.field_sublabel : ""}`,
                type: "category",
                entity: "delusions",
                options: sliderOptions

            }

            this.config.fields[delusion.field_name] = field
        }
    }

    async ngOnInit() {
        this.route.params.subscribe((params) => {
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

                this.http
                    .get('api/query-builder/querycontent/querygroup/' + queryId)
                    .subscribe((response: any) => {

                        this.contentComponent.items = response;
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
            case 'IS':
                return "IS"

            default:
                return null;
        }
    }

    convertTimeFrame(value: string) {

        switch (value) {
            case "6_months":
                return 'PAST_6_MONTH';
            case "1_months":
                return 'PAST_MONTH';
            case "1_years":
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
            const queryDTO: QueryDTO = {
                field: query.field?.toUpperCase() || '',
                operator: this.convertComparisonOperator(query.operator),
                timeFrame: this.convertTimeFrame(query.timeFame),
                value: query.value,
                entity: query.entity
            };

            return {
                query: queryDTO,
            };
        }
    }

    async saveQueryGroupToDB() {
        const query_group: QueryGroup = {
            name: this.queryGrouName,
            description: this.queryGroupDesc,
        };


        if (this.queryGroupId) {
            this.queryGroupId = await this.updateQueryGroup(query_group);
            await this.updateIndividualQueries();
        } else {
            this.queryGroupId = await this.saveNewQueryGroup(query_group)
            await this.saveIndividualQueries();
        }

        await this.saveContent();

        this.goBack();
    }

    async saveContent() {
        let content = this.contentComponent.items;

        await this.queryService.saveContent(this.queryGroupId, content);
    }

    saveNewQueryGroup(queryGroup: QueryGroup) {
        return this.http
            .post(this.baseUrl + '/querygroups', queryGroup).toPromise()


    }
    updateQueryGroup(queryGroup: QueryGroup) {
        return this.http
            .put(this.baseUrl + '/querygroups/' + this.queryGroupId, queryGroup).toPromise()

    }
    saveIndividualQueries() {
        const query_logic = {
            queryGroupId: this.queryGroupId,
            ...this.convertQuery(this.query),
        };

        return this.http
            .post(this.baseUrl + '/querylogic', query_logic).toPromise()

    }

    updateIndividualQueries() {
        const query_logic = {
            queryGroupId: this.queryGroupId,
            ...this.convertQuery(this.query),
        };

        return this.http
            .put(this.baseUrl + '/querylogic', query_logic).toPromise();

    }

    get isSaveButtonDisabled(): boolean {
        const hasName = !!this.queryGrouName;
        const hasDesc = !!this.queryGroupDesc;
        const hasQuery = this.query && this.query.rules.length > 0;

        return !(hasName && hasDesc && hasQuery);
    }
}
