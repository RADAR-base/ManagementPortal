import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { DeviceTypeDetailComponent } from '../../../../../../main/webapp/app/entities/device-type/device-type-detail.component';
import { DeviceTypeService } from '../../../../../../main/webapp/app/entities/device-type/device-type.service';
import { DeviceType } from '../../../../../../main/webapp/app/entities/device-type/device-type.model';

describe('Component Tests', () => {

    describe('DeviceType Management Detail Component', () => {
        let comp: DeviceTypeDetailComponent;
        let fixture: ComponentFixture<DeviceTypeDetailComponent>;
        let service: DeviceTypeService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [DeviceTypeDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({deviceTypeProducer: 'testProducer',
                            deviceTypeModel: 'testModel', catalogVersion: 'testVersion'})
                    },
                    DeviceTypeService,
                    EventManager
                ]
            }).overrideComponent(DeviceTypeDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(DeviceTypeDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(DeviceTypeService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new DeviceType(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testProducer', 'testModel', 'testVersion');
            expect(comp.deviceType).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
