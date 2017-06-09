import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { PatientDetailComponent } from '../../../../../../main/webapp/app/entities/patient/patient-detail.component';
import { PatientService } from '../../../../../../main/webapp/app/entities/patient/patient.service';
import { Patient } from '../../../../../../main/webapp/app/entities/patient/patient.model';

describe('Component Tests', () => {

    describe('Patient Management Detail Component', () => {
        let comp: PatientDetailComponent;
        let fixture: ComponentFixture<PatientDetailComponent>;
        let service: PatientService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [PatientDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({id: 123})
                    },
                    PatientService,
                    EventManager
                ]
            }).overrideComponent(PatientDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(PatientDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(PatientService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new Patient(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith(123);
            expect(comp.patient).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
