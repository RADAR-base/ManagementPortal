import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { SourceDataDetailComponent } from '../../../../../../main/webapp/app/entities/source-data/source-data-detail.component';
import { SourceDataService } from '../../../../../../main/webapp/app/entities/source-data/source-data.service';
import { SourceData } from '../../../../../../main/webapp/app/entities/source-data/source-data.model';

describe('Component Tests', () => {

    describe('SourceData Management Detail Component', () => {
        let comp: SourceDataDetailComponent;
        let fixture: ComponentFixture<SourceDataDetailComponent>;
        let service: SourceDataService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceDataDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sensorName: 'testSourceData'})
                    },
                    SourceDataService,
                    EventManager
                ]
            }).overrideComponent(SourceDataDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceDataDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceDataService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new SourceData(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testSourceData');
            expect(comp.sourceData).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
