import {
    Component, OnInit, OnDestroy, Input, OnChanges, SimpleChanges,
    SimpleChange
} from '@angular/core';
import { Response } from '@angular/http';
import { Subscription } from 'rxjs/Rx';
import { EventManager, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Source } from './source.model';
import { SourceService } from './source.service';
import { Principal } from '../../shared';
import {Project} from "../../entities/project/project.model";

@Component({
    selector: 'sources',
    templateUrl: './source.component.html'
})
export class SourceComponent implements OnInit, OnDestroy , OnChanges {

    @Input() project: Project;
    @Input() isProjectSpecific : boolean;

    sources: Source[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceService: SourceService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['source' , 'project' , 'projectStatus']);
    }

    ngOnInit() {
        console.log('init child')
        this.loadSources();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInDevices();
    }

    private loadSources() {
        if(this.project) {
            this.loadAllFromProject();
        }
        else {
            this.loadAll();
        }
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Source) {
        return item.id;
    }
    registerChangeInDevices() {
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification', (response) => this.loadSources());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
    loadAll() {
        this.sourceService.query().subscribe(
            (res: Response) => {
                this.sources = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    private loadAllFromProject() {
        this.sourceService.findAllByProject({
            projectId: this.project.id}).subscribe(
            (res: Response) => {
                this.sources = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project? changes.project: null;
        if(project){
            this.project = project.currentValue;
            this.loadAllFromProject();
        }
    }
}
