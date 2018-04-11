import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { Revision } from '../../entities/revision/revision.model';
import { RevisionService } from '../../entities/revision/revision.service';

@Component({
    selector: 'jhi-subject-revision',
    templateUrl: './subject-revision.component.html'
})
export class SubjectRevisionComponent implements OnInit, OnDestroy {

    subject: Subject;
    revision: Revision;
    private subscription: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private subjectService: SubjectService,
        private revisionService: RevisionService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['subject' , 'project' , 'projectStatus', 'audits']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['login'], params['revisionNb']);
        });
    }

    load(id, revisionNb) {
        this.subjectService.findForRevision(id, revisionNb).subscribe((subject) => {
            this.subject = subject;
        });
        this.revisionService.find(revisionNb).subscribe((revision) => {
            this.revision = revision;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
