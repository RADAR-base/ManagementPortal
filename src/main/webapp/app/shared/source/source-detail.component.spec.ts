import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../util/event-manager.service';
import { ManagementPortalTestModule } from '../util/test/test.module';
import { MockActivatedRoute } from '../util/test/mock-route.service';
import { SourceDetailComponent } from './source-detail.component';
import { SourceService } from './source.service';
import { Source } from './source.model';

describe('Component Tests', () => {

    describe('Source Management Detail Component', () => {
        let comp: SourceDetailComponent;
        let fixture: ComponentFixture<SourceDetailComponent>;
        let service: SourceService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sourceName: 'testSource'})
                    },
                    SourceService,
                    EventManager
                ]
            }).overrideTemplate(SourceDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of(new Source(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testSource');
            expect(comp.source).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
