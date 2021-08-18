import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ManagementPortalTestModule } from '../../shared/util/test/test.module';
import { PasswordComponent } from './password.component';
import { Password } from './password.service';
import { Principal } from '../../shared/auth/principal.service';
import { AccountService } from '../../shared/auth/account.service';

describe('Component Tests', () => {

    describe('PasswordComponent', () => {

        let comp: PasswordComponent;
        let fixture: ComponentFixture<PasswordComponent>;
        let service: Password;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [ManagementPortalTestModule],
                declarations: [PasswordComponent],
                providers: [
                    Principal,
                    AccountService,
                    Password
                ],
                schemas: [CUSTOM_ELEMENTS_SCHEMA],
            }).overrideTemplate(PasswordComponent, '').compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(PasswordComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(Password);
        });

        it('should show error if passwords do not match', () => {
            // GIVEN
            comp.password = 'password1$';
            comp.confirmPassword = 'password2$';
            // WHEN
            comp.changePassword();
            // THEN
            expect(comp.doNotMatch).toBe('ERROR');
            expect(comp.error).toBeNull();
            expect(comp.success).toBeNull();
        });

        it('should call Auth.changePassword when passwords match', () => {
            // GIVEN
            spyOn(service, 'save').and.returnValue(of(true));
            comp.password = comp.confirmPassword = 'myPassword1$';

            // WHEN
            comp.changePassword();

            // THEN
            expect(service.save).toHaveBeenCalledWith('myPassword1$');
        });

        it('should set success to OK upon success', function() {
            // GIVEN
            spyOn(service, 'save').and.returnValue(of(true));
            comp.password = comp.confirmPassword = 'myPassword1$';

            // WHEN
            comp.changePassword();

            // THEN
            expect(comp.doNotMatch).toBeNull();
            expect(comp.error).toBeNull();
            expect(comp.success).toBe('OK');
        });

        it('should notify of error if change password fails', function() {
            // GIVEN
            spyOn(service, 'save').and.returnValue(throwError('ERROR'));
            comp.password = comp.confirmPassword = 'myPassword1$';

            // WHEN
            comp.changePassword();

            // THEN
            expect(comp.doNotMatch).toBeNull();
            expect(comp.success).toBeNull();
            expect(comp.error).toBe('ERROR');
        });
    });
});
