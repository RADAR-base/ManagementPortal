import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { Patient } from './patient.model';
import { PatientService } from './patient.service';

@Component({
    selector: 'jhi-patient-detail',
    templateUrl: './patient-detail.component.html'
})
export class PatientDetailComponent implements OnInit, OnDestroy {

    patient: Patient;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private patientService: PatientService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['patient']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInPatients();
    }

    load(id) {
        this.patientService.find(id).subscribe((patient) => {
            this.patient = patient;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInPatients() {
        this.eventSubscriber = this.eventManager.subscribe('patientListModification', (response) => this.load(this.patient.id));
    }
}
