import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { ProjectDetailComponent } from '../../../../../../main/webapp/app/entities/project/project-detail.component';
import { Project, ProjectService } from '../../../../../../main/webapp/app/shared/project';

describe('Component Tests', () => {

    describe('Project Management Detail Component', () => {
        let comp: ProjectDetailComponent;
        let fixture: ComponentFixture<ProjectDetailComponent>;
        let service: ProjectService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [ProjectDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({projectName: 'testProject'})
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
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new Project(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testProject');
            expect(comp.project).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
