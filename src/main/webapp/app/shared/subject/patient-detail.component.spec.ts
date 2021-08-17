import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../util/event-manager.service';
import { ManagementPortalTestModule } from '../util/test/test.module';
import { MockActivatedRoute } from '../util/test/mock-route.service';
import { SubjectService } from './subject.service';
import { Subject } from './subject.model';
import { SubjectDetailComponent } from './subject-detail.component';

describe('Component Tests', () => {

    describe('Subject Management Detail Component', () => {
        let comp: SubjectDetailComponent;
        let fixture: ComponentFixture<SubjectDetailComponent>;
        let service: SubjectService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SubjectDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({login: 'test'})
                    },
                    SubjectService,
                    EventManager
                ]
            }).overrideTemplate(SubjectDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SubjectDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SubjectService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of(new Subject(10, 'test')));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('test');
            expect(comp.subject).toEqual(jasmine.objectContaining({id: 10, login: 'test'}));
            });
        });
    });

});
