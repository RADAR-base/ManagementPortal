import { browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('account', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));

    beforeAll(async() => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
    });

    beforeEach(async() => {
        browser.sleep(3000);
    });

    it('should fail to login with bad password', async() => {
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

    it('should login successfully with admin account', async() => {
        const expect1 = /login.title/;
        const modalTitle = element.all(by.css('.modal-content h1')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();
        browser.sleep(3000);

        const expect2 = /home.logged.message/;
        const successMessage = element.all(by.css('.alert-success span'));
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
    });

    it('should be able to update settings', async() => {
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

    it('should be able to update password', async() => {
        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnPasswordMenu();

        const expect1 = /password.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await password.sendKeys('newpassword');
        await element(by.id('confirmPassword')).sendKeys('newpassword');
        await element(by.css('button[type=submit]')).click();

        const expect2 = /password.messages.success/;
        const successMessage = element.all(by.css('.alert-success')).first();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignOut();

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignIn();

        await username.sendKeys('admin');
        await password.sendKeys('newpassword');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();

        await navBarPage.clickOnAccountMenu();
        await element(by.css('[routerLink="password"]')).click();
        // change back to default
        await password.clear();
        await password.sendKeys('admin');
        await element(by.id('confirmPassword')).clear();
        await element(by.id('confirmPassword')).sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
    });

});
