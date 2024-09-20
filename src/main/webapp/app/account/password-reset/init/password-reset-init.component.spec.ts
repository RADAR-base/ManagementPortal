import {ComponentFixture, inject, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';

import {ManagementPortalTestModule} from '../../../shared/util/test/test.module';
import {PasswordResetInitComponent} from './password-reset-init.component';
import {PasswordResetInit} from './password-reset-init.service';

describe('Component Tests', () => {

    describe('PasswordResetInitComponent', function () {
        let fixture: ComponentFixture<PasswordResetInitComponent>;
        let comp: PasswordResetInitComponent;

        beforeEach(() => {
            fixture = TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [PasswordResetInitComponent],
                providers: [
                    PasswordResetInit,
                ]
            }).createComponent(PasswordResetInitComponent);
            comp = fixture.componentInstance;
            comp.ngOnInit();
        });

        it('should define its initial state', function () {
            expect(comp.success).toBeUndefined();
            expect(comp.error).toBeUndefined();
            expect(comp.errorEmailNotExists).toBeUndefined();
            expect(comp.resetAccount).toEqual({});
        });

        it('sets focus after the view has been initialized', () => {
            fixture.detectChanges();
            let emailField = comp.emailField.nativeElement;
            spyOn(emailField, 'focus');
            comp.ngAfterViewInit();

            expect(emailField.focus).toHaveBeenCalled();
        });

        it('notifies of success upon successful requestReset',
            inject([PasswordResetInit], (service: PasswordResetInit) => {
                spyOn(service, 'save').and.returnValue(of({}));
                comp.resetAccount.email = 'user@domain.com';

                comp.requestReset();

                expect(service.save).toHaveBeenCalledWith('user@domain.com');
                expect(comp.success).toEqual('OK');
                expect(comp.error).toBeNull();
                expect(comp.errorEmailNotExists).toBeNull();
            })
        );

        it('notifies of unknown email upon email address not registered/400',
            inject([PasswordResetInit], (service: PasswordResetInit) => {
                spyOn(service, 'save').and.returnValue(throwError({
                    status: 400,
                    data: 'email address not registered'
                }));
                comp.resetAccount.email = 'user@domain.com';

                comp.requestReset();

                expect(service.save).toHaveBeenCalledWith('user@domain.com');
                expect(comp.success).toBeNull();
                expect(comp.error).toBeNull();
                expect(comp.errorEmailNotExists).toEqual('ERROR');
            })
        );

        it('notifies of error upon error response',
            inject([PasswordResetInit], (service: PasswordResetInit) => {
                spyOn(service, 'save').and.returnValue(throwError({
                    status: 503,
                    data: 'something else'
                }));
                comp.resetAccount.email = 'user@domain.com';

                comp.requestReset();

                expect(service.save).toHaveBeenCalledWith('user@domain.com');
                expect(comp.success).toBeNull();
                expect(comp.errorEmailNotExists).toBeNull();
                expect(comp.error).toEqual('ERROR');
            })
        );

    });
});
