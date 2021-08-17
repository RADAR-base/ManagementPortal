import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../../shared/util/event-manager.service';
import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { MockActivatedRoute } from '../../shared/util/test/mock-route.service';
import { SourceDataDetailComponent } from './source-data-detail.component';
import { SourceDataService } from './source-data.service';
import { SourceData } from './source-data.model';

describe('Component Tests', () => {

    describe('SourceData Management Detail Component', () => {
        let comp: SourceDataDetailComponent;
        let fixture: ComponentFixture<SourceDataDetailComponent>;
        let service: SourceDataService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceDataDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sourceDataName: 'testSourceData'})
                    },
                    SourceDataService,
                    EventManager
                ]
            }).overrideTemplate(SourceDataDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceDataDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceDataService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of(new SourceData(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testSourceData');
            expect(comp.sourceData).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
