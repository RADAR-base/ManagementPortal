import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { DeviceDetailComponent } from '../../../../../../main/webapp/app/entities/device/device-detail.component';
import { DeviceService } from '../../../../../../main/webapp/app/entities/device/device.service';
import { Device } from '../../../../../../main/webapp/app/entities/device/device.model';

describe('Component Tests', () => {

    describe('Device Management Detail Component', () => {
        let comp: DeviceDetailComponent;
        let fixture: ComponentFixture<DeviceDetailComponent>;
        let service: DeviceService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [DeviceDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({id: 123})
                    },
                    DeviceService,
                    EventManager
                ]
            }).overrideComponent(DeviceDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(DeviceDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(DeviceService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new Device(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith(123);
            expect(comp.device).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
