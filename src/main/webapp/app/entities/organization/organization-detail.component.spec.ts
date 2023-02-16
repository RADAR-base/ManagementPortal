import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../../shared/util/event-manager.service';
import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { MockActivatedRoute } from '../../shared/util/test/mock-route.service';
import { ProjectService } from '../../shared/project';
import { OrganizationDetailComponent } from './organization-detail.component';
import { first } from 'rxjs/operators';

describe('Component Tests', () => {

    describe('Organization Detail Component', () => {
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
                        useValue: new MockActivatedRoute({organizationName: 'testOrganization'})
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
            it('Should call load all on init', waitForAsync(async () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of({id: 10, organization: {name: 'test'}}));

            // WHEN
            comp.ngOnInit();

            // THEN
            const org = await comp.organization$.pipe(first()).toPromise();
            expect(org).toEqual(jasmine.objectContaining({id: 10, organization: {name: 'test'}}));

            expect(service.find).toHaveBeenCalledWith('testOrganization');

            }));
        });
    });
});
