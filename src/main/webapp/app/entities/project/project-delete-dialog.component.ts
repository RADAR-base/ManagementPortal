import {Component} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {ProjectPopupService} from './project-popup.service';

import {Project, ProjectService} from '../../shared';
import {EventManager} from '../../shared/util/event-manager.service';
import {Observable} from 'rxjs';
import {ObservablePopupComponent} from '../../shared/util/observable-popup.component';

@Component({
    selector: 'jhi-project-delete-dialog',
    templateUrl: './project-delete-dialog.component.html',
})
export class ProjectDeleteDialogComponent {

    project: Project;

    constructor(
        private projectService: ProjectService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(projectName: string) {
        this.projectService.delete(projectName).subscribe(() => {
            this.eventManager.broadcast({
                name: 'projectListModification',
                content: 'Deleted an project',
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-project-delete-popup',
    template: '',
})
export class ProjectDeletePopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private projectPopupService: ProjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.projectPopupService.open(ProjectDeleteDialogComponent, null, params['projectName']);
    }
}
