import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { SensorDataDetailComponent } from '../../../../../../main/webapp/app/entities/sensor-data/sensor-data-detail.component';
import { SensorDataService } from '../../../../../../main/webapp/app/entities/sensor-data/sensor-data.service';
import { SensorData } from '../../../../../../main/webapp/app/entities/sensor-data/sensor-data.model';

describe('Component Tests', () => {

    describe('SensorData Management Detail Component', () => {
        let comp: SensorDataDetailComponent;
        let fixture: ComponentFixture<SensorDataDetailComponent>;
        let service: SensorDataService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SensorDataDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sensorName: 'testSensorData'})
                    },
                    SensorDataService,
                    EventManager
                ]
            }).overrideComponent(SensorDataDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SensorDataDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SensorDataService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new SensorData(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testSensorData');
            expect(comp.sensorData).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
