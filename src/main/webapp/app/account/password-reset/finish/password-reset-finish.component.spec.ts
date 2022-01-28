import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { LoginModalService } from '../../../shared';
import { ManagementPortalTestModule } from '../../../shared/util/test/test.module';
import { MockActivatedRoute } from '../../../shared/util/test/mock-route.service';
import { PasswordResetFinishComponent } from './password-reset-finish.component';
import { PasswordResetFinish } from './password-reset-finish.service';
import { Password } from '../../password/password.service';

describe('Component Tests', () => {

    describe('PasswordResetFinishComponent', () => {

        let fixture: ComponentFixture<PasswordResetFinishComponent>;
        let comp: PasswordResetFinishComponent;

        beforeEach(() => {
            fixture = TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [PasswordResetFinishComponent],
                providers: [
                    PasswordResetFinish,
                    {
                        provide: LoginModalService,
                        useValue: null
                    },
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({'key': 'XYZPDQ'})
                    },
                    Password,
                ],
                schemas: [CUSTOM_ELEMENTS_SCHEMA],
            }).createComponent(PasswordResetFinishComponent);
            comp = fixture.componentInstance;
        });

        it('should define its initial state', function() {
            comp.ngOnInit();

            expect(comp.keyMissing).toBeFalsy();
            expect(comp.key).toEqual('XYZPDQ');
            expect(comp.resetAccount).toEqual({});
        });

        it('sets focus after the view has been initialized', () => {
            fixture.detectChanges();
            let passwordField = comp.passwordField.nativeElement;
            spyOn(passwordField, 'focus');
            comp.ngAfterViewInit();

            expect(passwordField.focus).toHaveBeenCalled();
        });
    });
});
