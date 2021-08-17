import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { EventManager } from '../../shared/util/event-manager.service';
import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { MockActivatedRoute } from '../../shared/util/test/mock-route.service';
import { SourceTypeDetailComponent } from './source-type-detail.component';
import { SourceTypeService } from './source-type.service';
import { SourceType } from './source-type.model';

describe('Component Tests', () => {

    describe('SourceType Management Detail Component', () => {
        let comp: SourceTypeDetailComponent;
        let fixture: ComponentFixture<SourceTypeDetailComponent>;
        let service: SourceTypeService;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceTypeDetailComponent],
                providers: [
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sourceTypeProducer: 'testProducer',
                            sourceTypeModel: 'testModel', catalogVersion: 'testVersion'})
                    },
                    SourceTypeService,
                    EventManager
                ]
            }).overrideTemplate(SourceTypeDetailComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceTypeDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceTypeService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(of(new SourceType(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testProducer', 'testModel', 'testVersion');
            expect(comp.sourceType).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
