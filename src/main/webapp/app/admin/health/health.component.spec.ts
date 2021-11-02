/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

import { JhiHealthCheckComponent } from './health.component';
import { JhiHealthService } from './health.service';
import { Health } from './health.model';

describe('HealthComponent', () => {
    let comp: JhiHealthCheckComponent;
    let fixture: ComponentFixture<JhiHealthCheckComponent>;
    let service: JhiHealthService;

    beforeEach(
      waitForAsync(() => {
          TestBed.configureTestingModule({
              imports: [HttpClientTestingModule],
              declarations: [JhiHealthCheckComponent],
          })
          .overrideTemplate(JhiHealthCheckComponent, '')
          .compileComponents();
      })
    );

    beforeEach(() => {
        fixture = TestBed.createComponent(JhiHealthCheckComponent);
        comp = fixture.componentInstance;
        service = TestBed.inject(JhiHealthService);
    });

    describe('getBadgeClass', () => {
        it('should get badge class', () => {
            const upBadgeClass = comp.getBadgeClass('UP');
            const downBadgeClass = comp.getBadgeClass('DOWN');
            expect(upBadgeClass).toEqual('badge-success');
            expect(downBadgeClass).toEqual('badge-danger');
        });
    });

    describe('refresh', () => {
        it('should call refresh on init', () => {
            // GIVEN
            const health: Health = { status: 'UP', components: { mail: { status: 'UP', details: { mailDetail: 'mail' } } } };
            spyOn(service, 'checkHealth').and.returnValue(of(health));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.checkHealth).toHaveBeenCalled();
            expect(comp.health).toEqual(health);
        });

        it('should handle a 503 on refreshing health data', () => {
            // GIVEN
            const health: Health = { status: 'DOWN', components: { mail: { status: 'DOWN' } } };
            spyOn(service, 'checkHealth').and.returnValue(throwError(new HttpErrorResponse({ status: 503, error: health })));

            // WHEN
            comp.refresh();

            // THEN
            expect(service.checkHealth).toHaveBeenCalled();
            expect(comp.health).toEqual(health);
        });
    });
});
