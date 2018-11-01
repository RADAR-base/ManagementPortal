import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import {SubjectService} from "../../../../../../main/webapp/app/shared/subject/subject.service";
import {Subject} from "../../../../../../main/webapp/app/shared/subject/subject.model";
import {SubjectDetailComponent} from "../../../../../../main/webapp/app/shared/subject/subject-detail.component";

describe('Component Tests', () => {

    describe('Subject Management Detail Component', () => {
        let comp: SubjectDetailComponent;
        let fixture: ComponentFixture<SubjectDetailComponent>;
        let service: SubjectService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SubjectDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({login: "test"})
                    },
                    SubjectService,
                    EventManager
                ]
            }).overrideComponent(SubjectDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SubjectDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SubjectService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new Subject(10, "test")));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith("test");
            expect(comp.subject).toEqual(jasmine.objectContaining({id: 10, login:"test"}));
            });
        });
    });

});
