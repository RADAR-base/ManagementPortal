import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { SourceTypeDetailComponent } from '../../../../../../main/webapp/app/entities/source-type/source-type-detail.component';
import { SourceTypeService } from '../../../../../../main/webapp/app/entities/source-type/source-type.service';
import { SourceType } from '../../../../../../main/webapp/app/entities/source-type/source-type.model';

describe('Component Tests', () => {

    describe('SourceType Management Detail Component', () => {
        let comp: SourceTypeDetailComponent;
        let fixture: ComponentFixture<SourceTypeDetailComponent>;
        let service: SourceTypeService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceTypeDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sourceTypeProducer: 'testProducer',
                            sourceTypeModel: 'testModel', catalogVersion: 'testVersion'})
                    },
                    SourceTypeService,
                    EventManager
                ]
            }).overrideComponent(SourceTypeDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceTypeDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceTypeService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new SourceType(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testProducer', 'testModel', 'testVersion');
            expect(comp.sourceType).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
