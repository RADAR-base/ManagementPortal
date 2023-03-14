import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../../shared/util/event-manager.service';
import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { MockActivatedRoute, MockRouter } from '../../shared/util/test/mock-route.service';
import { ProjectService } from '../../shared/project';
import { ProjectDetailComponent } from './project-detail.component';
import { filter, first } from 'rxjs/operators';

describe('Component Tests', () => {

    describe('Project Management Detail Component', () => {
        let comp: ProjectDetailComponent;
        let fixture: ComponentFixture<ProjectDetailComponent>;
        let service: ProjectService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [ProjectDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({projectName: 'testProject'})
                    },
                    {
                        provide: Router,
                        useClass: MockRouter,
                    },
                    ProjectService,
                    EventManager
                ]
            }).overrideTemplate(ProjectDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(ProjectDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(ProjectService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', waitForAsync(async () => {
                // GIVEN
                spyOn(service, 'find').and.returnValue(of({id: 10, organization: {name: 'test'}}));

                // WHEN
                comp.ngOnInit();

                // THEN
                expect(service.find).toHaveBeenCalledWith('testProject');
                const result = await comp.project$.pipe(filter(p => !!p), first()).toPromise();
                expect(result).toEqual(jasmine.objectContaining({id: 10, organization: {name: 'test'}}))
            }));
        });
    });
});
