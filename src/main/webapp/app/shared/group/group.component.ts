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
import {Router} from "@angular/router";

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
            private router: Router,
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
        this.registerChangeInGroups();
    }

    ngOnDestroy() {
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

    deleteGroup(group: Group) {
        this.groupService.delete(this.project.projectName, group.name).subscribe(
            () => {
                this.eventManager.broadcast({name: 'groupListModification', content: null});
            },
            (error) => {
                if(error.status === 409){
                    this.router.navigate(['/', { outlets: { popup: 'project-group/'+ this.project.projectName + '/' + group.id + '/delete'} }])
                }
            }
        );
    }
}
