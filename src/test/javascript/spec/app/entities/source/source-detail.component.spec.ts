import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils, EventManager } from 'ng-jhipster';
import { ManagementPortalTestModule } from '../../../test.module';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import {SourceDetailComponent} from "../../../../../../main/webapp/app/shared/source/source-detail.component";
import {SourceService} from "../../../../../../main/webapp/app/shared/source/source.service";
import {Source} from "../../../../../../main/webapp/app/shared/source/source.model";

describe('Component Tests', () => {

    describe('Source Management Detail Component', () => {
        let comp: SourceDetailComponent;
        let fixture: ComponentFixture<SourceDetailComponent>;
        let service: SourceService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [SourceDetailComponent],
                providers: [
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({sourceName: 'testSource'})
                    },
                    SourceService,
                    EventManager
                ]
            }).overrideComponent(SourceDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SourceDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SourceService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN

            spyOn(service, 'find').and.returnValue(Observable.of(new Source(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith('testSource');
            expect(comp.source).toEqual(jasmine.objectContaining({id:10}));
            });
        });
    });

});
