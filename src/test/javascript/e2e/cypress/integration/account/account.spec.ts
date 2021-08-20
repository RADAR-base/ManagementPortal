import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('account', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should fail to login with bad password', () => {
        const expect1 = /home.title/;
        const header = element.all(by.css('h1')).first();
        expect((await header.getAttribute('jhiTranslate'))).toMatch(expect1);

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignIn();

        await username.sendKeys('admin');
        await password.sendKeys('foo');
        await element(by.css('button[type=submit]')).click();

        const expect2 = /login.messages.error.authentication/;
        const alertMessage = element.all(by.css('.alert-danger')).first();
        expect((await alertMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
        await username.clear();
        await password.clear();
    });

    it('should login successfully with admin account', () => {
        const expect1 = /login.title/;
        const modalTitle = element.all(by.css('.modal-content h1')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();
        browser.sleep(3000);

        const successMessage = element.all(by.css('.alert-success span'));
        expect((await successMessage.isPresent()));
    });

    it('should be able to update settings', () => {
        await navBarPage.clickOnAccountMenu();
        await element(by.css('[routerLink="settings"]')).click();

        const expect1 = /settings.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await element(by.css('button[type=submit]')).click();

        const expect2 = /settings.messages.success/;
        const successMessage = element.all(by.css('.alert-success')).first();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
    });

    it('should be able to update password', () => {
        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnPasswordMenu();

        const expect1 = /password.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await password.sendKeys('newpassword123$');
        await element(by.id('confirmPassword')).sendKeys('newpassword123$');
        await element(by.css('button[type=submit]')).click();

        const expect2 = /password.messages.success/;
        const successMessage = element.all(by.css('.alert-success')).first();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignOut();

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignIn();

        await username.sendKeys('admin');
        await password.sendKeys('newpassword123$');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();
        browser.sleep(3000);

        const successElements = element.all(by.css('.alert-success span'));
        expect((await successElements.isPresent()));
    });
});
