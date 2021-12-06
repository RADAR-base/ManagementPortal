import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../../shared/util/event-manager.service';
import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { MockActivatedRoute } from '../../shared/util/test/mock-route.service';
import { Project, ProjectService } from '../../shared/project';
import { OrganizationDetailComponent } from './organization-detail.component';

describe('Component Tests', () => {

    describe('Project Management Detail Component', () => {
        let comp: OrganizationDetailComponent;
        let fixture: ComponentFixture<OrganizationDetailComponent>;
        let service: ProjectService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [OrganizationDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({projectName: 'testProject'})
                    },
                    ProjectService,
                    EventManager
                ]
            }).overrideTemplate(OrganizationDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(OrganizationDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(ProjectService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of(new Project(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testProject');
            expect(comp.organization).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});