import { Component, OnInit } from '@angular/core';

import Chart from 'chart.js/auto';
import { ChartConfiguration, registerables } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
Chart.register(...registerables, ChartDataLabels);
import { Graph } from './types';
import { SubjectService } from '../subject.service';
import { ActivatedRoute } from '@angular/router';
import { Subject } from '../subject.model';
import { HttpResponse } from '@angular/common/http';

const domainGraph: Graph = {
    type: 'line',
    showScaleY: true,
    showDataTables: true,
};

const barGraph: Graph = {
    type: 'bar',
    showScaleY: false,
    showDataTables: true,
};

const lineGraph: Graph = {
    type: 'line',
    showScaleY: false,
    showDataTables: true,
};

const histogramGraph: Graph = {
    type: 'histogram',
    showScaleY: false,
    showDataTables: true,
};
const socialMap = {
    '1': 'On my own',
    '2': 'With strangers',
    '3': 'With people I know',
    '4': 'With people I am close to',
    '5': 'Connecting with people online or using social media',
};

@Component({
    selector: 'app-data-summary',
    templateUrl: './data-summary.component.html',
    styleUrls: ['./data-summary.component.scss'],
})
export class DataSummaryComponent implements OnInit {
    title = 'ng-chart';
    chart: any = [];
    chart_heart_rate: any = [];
    chart_heart_rate_variability: any = [];
    questionnaireDomains: any = [''];
    chart_type: { [id: string]: Graph } = {
        delusion: domainGraph,
        negative_emotions: domainGraph,
        positive_emotions: domainGraph,
        dissociation: domainGraph,
        stress: domainGraph,
        sleep_period: domainGraph,
        hope: domainGraph,
        mood: domainGraph,
        anxiety: domainGraph,
        self_esteem: domainGraph,
        connectedness: domainGraph,
        coping: domainGraph,
        fear: domainGraph,
        hallucination_hear: domainGraph,
        hallucination_vision: domainGraph,
        threat: domainGraph,

        questionnaire: barGraph,
        heart_rate: lineGraph,
        hrv: lineGraph,

        steps: barGraph,
        activity: lineGraph,
        respiratory_rate: lineGraph,
        screen_usage: lineGraph,

        // ugly...sorry..no time
        social: histogramGraph,

        social_1: barGraph,
        social_2: barGraph,
        social_3: barGraph,
        social_4: barGraph,
        social_5: barGraph,

        sleep: histogramGraph,
        sleep_1: barGraph,
        sleep_2: barGraph,
        sleep_3: barGraph,
        sleep_4: barGraph,
        sleep_5: barGraph,
        sleep_6: barGraph,
        sleep_7: barGraph,
        sleep_8: barGraph,

        wherearebout: histogramGraph,
        wherearebout_1: barGraph,
        wherearebout_2: barGraph,
        wherearebout_3: barGraph,
        wherearebout_4: barGraph,
        wherearebout_5: barGraph,
        wherearebout_6: barGraph,
        wherearebout_7: barGraph,
        wherearebout_8: barGraph,
        wherearebout_9: barGraph,
        wherearebout_10: barGraph,
        wherearebout_11: barGraph,

        delusion_1: domainGraph,
        delusion_2: domainGraph,
        delusion_3: domainGraph,
        delusion_4: domainGraph,
        delusion_5: domainGraph,
        delusion_6: domainGraph,
        delusion_7: domainGraph,
        delusion_8: domainGraph,
        delusion_9: domainGraph,
        delusion_10: domainGraph,
        delusion_11: domainGraph,
        delusion_12: domainGraph,
    };
    isDataSummaryReady = false;
    data: any = {};
    monthLabelsPerGraph: any = {};
    charts: any = {};
    monthLabels: string[] = [];

    questionnaireTotal: number = 0;
    questionnaireAverage: number = 0;
    averages = {};

    stepsTotal: number = 0;
    stepsAverage: number = 0;

    questionnaireKey: string = 'questionnaire';
    socialMap = {
        '1': 'On my own',
        '2': 'With strangers',
        '3': 'With people I know',
        '4': 'With people I am close to',
        '5': 'Connecting with people online or using social media',
    };

    socialLabels = [
        'On my own',
        'With strangers',
        'With people I know',
        'With people I am close to',
        'Connecting with people online or using social media',
    ];
    socialKeys = Object.keys(this.socialMap);

    sleepMap = {
        '0-2': '0-2 hrs',
        '2-4': '2-4 hrs',
        '4-6': '4-6 hrs',
        '6-8': '6-8 hrs',
        '8-10': '8-10 hrs',
        '10-12': '10-12 hrs',
        '12-14': '12-14 hrs',
        '14+': '14+ hrs+',
    };

    sleepLabels = [
        '0-2 hrs',
        '2-4 hrs',
        '4-6 hrs',
        '6-8 hrs',
        '8-10 hrs',
        '10-12 hrs',
        '12-14 hrs',
        '14+',
    ];

    sleepMapKeys = Object.keys(this.sleepMap);

    whereaboutsMap = {
        '1': 'Relaxing (e.g. watching TV, reading a book, resting, other)',
        '2': 'Working / at School',
        '3': 'Studying',
        '4': 'Housekeeping',
        '5': 'Shopping',
        '6': 'Hygiene / Self-care activity',
        '7': 'Eating/Drinking',
        '8': 'Travelling',
        '9': 'Exercising (e.g. walking, jogging, dancing, playing sport, other)',
        '10': 'Leisure Activity (e.g. going to an event, visiting a museum, cinema or library, etc)',
        '11': 'Nothing',
    };

    whereaboutsLabels = [
        'Relaxing (e.g. watching TV, reading a book, resting, other)',
        'Working / at School',
        'Studying',
        'Housekeeping',
        'Shopping',
        'Hygiene / Self-care activity',
        'Eating/Drinking',
        'Travelling',
        'Exercising (e.g. walking, jogging, dancing, playing sport, other)',
        'Leisure Activity (e.g. going to an event, visiting a museum, cinema or library, etc)',
        'Nothing',
    ];

    delusions = {
        delusion_1: "I have felt like I could read other people's thoughts",
        delusion_2: 'I have felt like other people were reading my thoughts',
        delusion_3:
            'I have felt that my thoughts were being controlled or influenced',
        delusion_4: 'I have felt like my thoughts were alien to me in some way',
        delusion_5: 'I have felt like the world is not real',
        delusion_6: 'I have felt like I am not real',
        delusion_7: 'I have felt like people were not what they seemed',
        delusion_8:
            'I have felt like things on the TV, in books or magazines had a special meaning for me',
        delusion_9: 'I have felt like there was a conspiracy against me',
        delusion_10: 'I have been jealous',
        delusion_11: 'I have felt like something bad was about to happen',
        delusion_12:
            'I have felt distinctly concerned about my physical health',
    };

    delusionKeys = Object.keys(this.delusions);

    histogramLabels = { social: [], sleep: [], wherearebout: [] };
    whereaboutsMapKeys = Object.keys(this.whereaboutsMap);

    subject: Subject;
    private subscription: any;

    constructor(
        private subjectService: SubjectService,
        private route: ActivatedRoute
    ) {}

    createMonthHistogram(key: string, months: string[], binLabels2: string[]) {
        const binColors = [
            'rgba(255, 99, 132, 0.6)',
            'rgba(54, 162, 235, 0.6)',
            'rgba(255, 206, 86, 0.6)',
            'rgba(75, 192, 192, 0.6)',
            'rgba(153, 102, 255, 0.6)',
            'rgba(255, 159, 64, 0.6)',
            'rgba(0, 204, 102, 0.6)',
            'rgba(204, 0, 204, 0.6)',
            'rgba(102, 102, 102, 0.6)',
            'rgba(0, 153, 255, 0.6)',
            'rgba(255, 51, 153, 0.6)',
        ];

        let activeColors = binColors.slice(0, binLabels2.length);

        const data = this.data[key];

        return {
            type: 'bar',
            data: {
                labels: months,
                datasets: binLabels2.map((label, index) => ({
                    label: label,
                    data: data[index],
                    backgroundColor: activeColors[index],
                    borderColor: activeColors[index].replace('0.6', '1'),
                    borderWidth: 1,
                })),
            },
            options: {
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                },
                plugins: {
                    datalabels: {
                        display: false,
                    },
                },
            },
        };
    }

    createLineChart(
        id: string,
        labelsP: string[],
        dataP: number[],
        showScaleY: boolean,
        showDataLables: boolean,
        color: string = 'rgba(110, 37, 147, 0.9)'
    ) {
        // this is done for graphs which has only one value
        // let labels = labelsP.push('');
        // let data = dataP.push(0);
        let ticks = {};

        if (showScaleY) {
            ticks = {
                max: 7, // End at 7
                ticks: {
                    stepSize: 1, // Show every integer from 1 to 7
                },
            };
        }

        let graphObject = {
            type: 'line',
            data: {
                labels: labelsP,
                datasets: [
                    {
                        fill: true,
                        tension: 0.2,
                        data: dataP,
                        borderWidth: 2,
                        borderColor: color,
                    },
                ],
            },
            options: {
                devicePixelRatio: 4,
                layout: {
                    padding: {
                        left: 30,
                        right: 30,
                        top: 30,
                        bottom: 30,
                    },
                },
                responsive: false,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        type: 'linear',
                        beginAtZero: true,
                        display: showScaleY,
                        ...ticks,
                    },
                },
                plugins: {
                    title: {
                        display: false,
                    },
                    legend: {
                        display: false,
                        position: 'top',
                    },
                    datalabels: {
                        display: showDataLables,
                        align: 'top',
                        formatter: (value) => value,
                        color: '#000',
                        font: {
                            weight: 'bold',
                            size: 10,
                        },
                    },
                },
            },
        };

        if (showDataLables) {
            graphObject['options']['datalabels'] = {
                align: 'top',

                formatter: (value) => value,
                color: '#000',
                font: {
                    weight: 'bold',
                    size: 12,
                },
            };
        } else {
            graphObject['options']['datalabels'] = {
                display: false,
            };
        }

        return graphObject;
    }

    createBarChart(
        id: string,
        labels: string[],
        data: number[],
        barColor: string = 'rgba(191,	236,	235	, 1)'
    ) {
        return {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        data: data,
                        borderWidth: 1,
                        borderRadius: 5,
                        barThickness: 30,
                        backgroundColor: [barColor],
                    },
                ],
            },
            options: {
                devicePixelRatio: 4,
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        display: false,
                    },
                },
                plugins: {
                    title: {
                        display: false,
                    },
                    legend: {
                        display: false,
                        position: 'top',
                    },
                    datalabels: {
                        anchor: 'end',

                        formatter: (value) => value,
                        color: '#000',
                        font: {
                            weight: 'bold',
                            size: 8,
                        },
                        offset: -20,
                        align: 'top',
                    },
                },
            },
        };
    }

    formatMonth(monthString: string) {
        const [year, month] = monthString.split('-').map(Number);
        const date = new Date(year, month - 1);

        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
            year: '2-digit', // This ensures a 2-digit year format
        }).format(date);
    }

    // this only applies to Questionnaires and Steps
    calculateTotalsAndAverage() {
        let questionnaireData = this.data[this.questionnaireKey];

        if (questionnaireData && questionnaireData.length > 0) {
            this.questionnaireTotal = questionnaireData.reduce(
                (acc, num) => acc + num,
                0
            );

            this.questionnaireAverage = this.questionnaireTotal / 12;

            this.questionnaireAverage = Number(
                this.questionnaireAverage.toFixed(1)
            );
        }

        let stepsData = this.data['steps'];

        if (stepsData && stepsData.length > 0) {
            this.stepsTotal = stepsData.reduce(
                (acc, num) => Number(acc) + Number(num),
                0
            );

            this.stepsAverage = this.stepsTotal / 12;

            this.stepsAverage = Number(this.stepsAverage.toFixed(1));
        }

        let heartRate = this.data['heart_rate'];
        if (heartRate && heartRate.length > 0) {
            var heartRateTotal = heartRate.reduce(
                (acc, num) => Number(acc) + Number(num),
                0
            );

            let numberOfValues = 0;
            heartRate.forEach((value) => {
                numberOfValues += value > 0 ? 1 : 0;
            })

            this.averages['heart_rate'] = (Number(heartRateTotal) / numberOfValues).toFixed(
                1
            );
        }
    }

    addMonthPerKey(key: string, month: string) {
        if (this.monthLabelsPerGraph[key] == undefined) {
            this.monthLabelsPerGraph[key] = [];
        }
        this.monthLabelsPerGraph[key].push(this.formatMonth(month));
    }
    pushToData(key: string, value: number | string) {
        if (this.data[key] == undefined) {
            this.data[key] = [];
        }
        this.data[key].push(value);
    }

    cleanupEmptyData() {
        Object.keys(this.data).forEach((key) => {
            if (
                Array.isArray(this.data[key]) &&
                this.data[key].every((num) => num === 0)
            ) {
                delete this.data[key]; // Remove the key if the array contains only 0s
            }
        });
    }

    pushHistogramData(key: string, dataArray: number[]) {
        if (this.data[key] == undefined) {
            this.data[key] = [];
        }
        this.data[key].push(dataArray);
    }

    generateMonths(startMonth) {
        let result = [];
        let [year, month] = startMonth.split('-').map(Number);

        for (let i = 0; i <= 12; i++) {
            let newMonth = ((month + i - 1) % 12) + 1;
            let newYear = year + Math.floor((month + i - 1) / 12);
            result.push(`${newYear}-${String(newMonth).padStart(2, '0')}`);
        }

        return result;
    }

    loadData(response: HttpResponse<any>) {
        if (!response.body) {
            return false;
        }
        const allData = response.body.data;
        const allPhysical = response.body.allPhysical;
        const allSlider = response.body.allSlider;
        console.log('all data', response.body);
        const months = Object.keys(allData).sort();
        let allMonths = this.generateMonths(months[0]);

        // march , april ... next year

        // generates for 12 months and goes throuhg each month
        // if there is no file for that month, append 0, basically empty month
        // otherwise process the data
        allMonths.forEach((month) => {
            const data = allData[month];
            this.monthLabels.push(this.formatMonth(month));

            if (data) {
                delete data.questionnaire_slider['delusion_1'];
            }

            // this goes through the slider data (questionnaire_responses/slider in export from James)
            // if there is a data add it to this.data
            // add corresponding month
            // data looks like this basically
            // this.data [1,2,2,3,4,5,6,7,8,9,10,11]
            // months [Apr, Mar, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb]
            // this is then fed to createGraphs and creates an object ready for chart.js

            allSlider.forEach((sliderKey) => {
                if (data) {
                    const sliderData = data.questionnaire_slider[sliderKey];
                    if (sliderData) {
                        this.pushToData(
                            sliderKey,
                            Number(sliderData).toFixed(1)
                        );
                        this.addMonthPerKey(sliderKey, month);
                        return;
                    }
                }

                // no data then add empty data
                this.pushToData(sliderKey, 0);
                this.addMonthPerKey(sliderKey, month);
            });

            // similar like above but this goes through physical data (heart rate etc)
            //but same principles

            allPhysical.forEach((sliderKey) => {
                if (data) {
                    const sliderData = data.physical[sliderKey];
                    if (sliderData) {
                        this.pushToData(
                            sliderKey,
                            Number(sliderData).toFixed(1)
                        );
                        this.addMonthPerKey(sliderKey, month);
                        return;
                    }
                }

                // no data then add empty data
                this.pushToData(sliderKey, 0);
                this.addMonthPerKey(sliderKey, month);
            });

            let questionnaireKey = 'questionnaire';
            if (data) {
                // gets data for the questionare (number of questionnaires per month)

                this.pushToData(
                    questionnaireKey,
                    Number(data.questionnaire_total)
                );

                this.addMonthPerKey(questionnaireKey, month);

                // processes the histograms for social, whereabout and sleep
                this.processHistogramData(
                    'social',
                    Object.keys(this.socialMap),
                    data.questionnaire_histogram.social,
                    month
                );

                this.processHistogramData(
                    'wherearebout',
                    Object.keys(this.whereaboutsMap),
                    data.questionnaire_histogram.whereabouts,
                    month
                );

                this.processHistogramData(
                    'sleep',
                    Object.keys(this.sleepMap),
                    data.questionnaire_histogram.sleep,
                    month
                );
            } else {
                // if no data (there is no file for a particualr month), add 0 to the this.data
                this.pushToData(questionnaireKey, 0);

                this.addMonthPerKey(questionnaireKey, month);

                this.socialKeys.forEach((key) => {
                    this.pushToData('social_' + key, 0);
                    this.addMonthPerKey('social_' + key, month);
                });

                this.whereaboutsMapKeys.forEach((key) => {
                    this.pushToData('wherearebout_' + key, 0);
                    this.addMonthPerKey('wherearebout_' + key, month);
                });

                this.sleepMapKeys.forEach((key, index) => {
                    let id = index + 1;
                    this.pushToData('sleep_' + id, 0);
                    this.addMonthPerKey('sleep_' + id, month);
                });
            }

            this.addMonthPerKey('social', month);
            this.addMonthPerKey('wherearebout', month);
            this.addMonthPerKey('sleep', month);
        });

        // calculates total averages for steps, heart rate, questionniare. Any other similar calculations would go here
        this.calculateTotalsAndAverage();

        // clean any data that has just 0s. Sometimes this.data might have an array [0,0,0,0,0,0,0,0,0,0,0,0] for a key
        // if all values are 0 we don't want to display an empty graph. This will remove those keys from the data
        this.cleanupEmptyData();

        // if there is less than 4 categories, we will display histogram as just one graph instead of it being split
        // this would be used to split the histogram into multiple graphs (currently histogram is one graph, but if it is not readbale this can be used)
        this.createHistogramsIfNecessary(
            'social',
            this.socialKeys,
            this.socialLabels
        );
        this.createHistogramsIfNecessary(
            'sleep',
            this.sleepMapKeys,
            this.sleepLabels
        );
        this.createHistogramsIfNecessary(
            'wherearebout',
            this.whereaboutsMapKeys,
            this.whereaboutsLabels
        );

        console.log('this.data', this.data);
        console.log('this month', this.monthLabelsPerGraph);
    }

    processHistogramData(
        domainkey: string,
        mapKeys: string[],
        data: any,
        month: string
    ) {
        mapKeys.forEach((key, index) => {
            let id = index + 1;
            let identifier = `${domainkey}_` + id;
            let dataValue = data[key];

            this.pushToData(identifier, dataValue ?? 0);
            this.addMonthPerKey(identifier, month);
        });
    }

    createHistogramsIfNecessary(
        dataKey: string,
        allKeys: string[],
        labels: string[]
    ) {
        let numberOfGraphsWithData = 0;

        let numberOfMonths = this.monthLabelsPerGraph[dataKey].length;
        allKeys.forEach((key, index) => {
            let exists = this.data[`${dataKey}_` + (index + 1)];
            if (exists) {
                numberOfGraphsWithData++;
            }
        });

        this.data[dataKey] = [];

        // if you set this to 4 then if a histogram has more than 4 categories I think it will split the graph)
        if (numberOfGraphsWithData > 0) {
            allKeys.forEach((key, index) => {
                let exists = this.data[`${dataKey}_` + (index + 1)];

                if (exists) {
                    this.histogramLabels[dataKey].push(labels[index]);
                    this.data[`${dataKey}`].push([...exists]);
                }

                delete this.data[`${dataKey}_` + (index + 1)];
            });
        }
    }

    // this will just take the stuff in this.data and this.monthLabelsPerGRaph and feed it into functions to create objects compatible with chart.js
    createGraphs() {
        let weekLabels = [] as string[];
        for (let i = 1; i <= 52; i++) {
            weekLabels.push('Week ' + i);
        }

        for (const [key, value] of Object.entries(this.data)) {
            let values = value as number[];
            let chartType = this.chart_type[key];

            if (chartType.type == 'line') {
                let labeles =
                    chartType.timeframe == 'week'
                        ? weekLabels
                        : this.monthLabelsPerGraph[key];
                this.charts[key] = this.createLineChart(
                    key,
                    this.monthLabelsPerGraph[key],
                    values,
                    chartType.showScaleY,
                    chartType.showDataTables,
                    chartType.color
                );
            } else if (chartType.type == 'histogram') {
                let labels = this.monthLabelsPerGraph[key + '_1'];
                let binLabels: any = null;

                // if (key == 'social') {
                //     binLabels = this.socialLabels;
                // } else if (key == 'sleep') {
                //     binLabels = this.sleepLabels;
                // } else {
                //     binLabels = this.whereaboutsLabels;
                // }
                binLabels = this.histogramLabels[key];

                this.charts[key] = this.createMonthHistogram(
                    key,
                    labels,
                    binLabels
                );
            } else {
                this.charts[key] = this.createBarChart(
                    key,
                    this.monthLabelsPerGraph[key],
                    values,
                    chartType.color
                );
            }
        }
    }
    loadSubject(id) {
        this.subjectService.find(id).subscribe((subject: Subject) => {
            this.subject = subject;

            if (subject)
                this.subjectService
                    .findDataSummary(subject!.login!)
                    .subscribe((response: HttpResponse<any>) => {
                        let result = this.loadData(response);

                        if (result == false) {
                            this.isDataSummaryReady = false;
                        } else {
                            this.isDataSummaryReady = true;
                            this.createGraphs();
                        }
                    });
        });
    }
    async ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.loadSubject(params['login']);
        });
    }
}
