import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Subscription } from 'rxjs';

import {Group, GroupService, Project} from '..';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';

@Component({
    selector: 'jhi-groups',
    templateUrl: './group.component.html',
    styleUrls: ['./group.component.scss'],
})
export class GroupComponent implements OnInit, OnDestroy, OnChanges {
    project$ = new BehaviorSubject<Project>(null);

    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }

    groups: Group[];
    eventSubscriber: Subscription;

    constructor(
            private groupService: GroupService,
            private alertService: AlertService,
            private eventManager: EventManager,
    ) {
        this.groups = [];
    }

    loadGroups() {
        this.groupService.list(this.project.projectName).subscribe(
        (res: Group[]) => this.groups = res,
        (res: HttpErrorResponse) => this.alertService.error(res.message, null, null),
        );
    }

    ngOnInit() {
        this.loadGroups();
        this.registerChangeInGroups();
    }

    ngOnDestroy() {
        this.project$.complete();
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Group) {
        return item.id;
    }

    registerChangeInGroups() {
        this.eventSubscriber = this.eventManager.subscribe('groupListModification', (_) => {
            this.loadGroups()
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        this.groups = [];
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
            this.loadGroups();
        }
    }

}
