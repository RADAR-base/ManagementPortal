import { $, browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('OAuth Clients e2e test', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));

    beforeAll(async () => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignIn();

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
        await browser.waitForAngular();
    });

    beforeEach(async () => {
        browser.sleep(1000);
    });

    it('should load OAuth clients', async () => {
        await navBarPage.clickOnAdminMenu();
        await element.all(by.css('[routerLink="oauth-client"]')).first().click();

        const expectVal = /managementPortalApp.oauthClient.home.title/;
        const pageTitle = element.all(by.css('h4 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);
    });

    it('should load create OAuth Client dialog', async () => {
        await element(by.css('jhi-oauth-client h4 button.btn-primary')).click();
        const expectVal = /managementPortalApp.oauthClient.home.createOrEditLabel/;
        const modalTitle = element.all(by.css('h4.modal-title')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);

        await element(by.css('button.close')).click();
    });

    it('should disable edit and delete buttons for protected clients', async () => {
        // find the table row that contains the protected badge, and assert it contains zero enabled buttons
        expect((await element(by.cssContainingText('span.badge-info', 'protected: true')).element(by.xpath('ancestor::tr'))
            .all(by.css('button')).filter((button) => button.isEnabled()).count())).toEqual(2);
        // show more, show less buttons are enabled
    });

    it('should be able to create OAuth Client', async () => {
        await element(by.css('jhi-oauth-client h4 button.btn-primary')).click();
        await element(by.id('id')).sendKeys('test-client');
        await element(by.cssContainingText('button', 'Random')).click();

        expect((await element(by.id('secret')).getAttribute('value')).length).toBeGreaterThan(0);

        await element(by.id('scope')).sendKeys('SUBJECT.READ');
        await element(by.id('resourceIds')).sendKeys('res_ManagementPortal');
        await element(by.cssContainingText('label.form-check-label', 'refresh_token')).element(by.css('input')).click();
        await element(by.cssContainingText('label.form-check-label', 'password')).element(by.css('input')).click();
        await element(by.id('accessTokenValidity')).clear();
        await element(by.id('accessTokenValidity')).sendKeys('3600');
        await element(by.id('refreshTokenValidity')).clear();
        await element(by.id('refreshTokenValidity')).sendKeys('7200');
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
    });

    it('should be able to edit OAuth Client', async () => {
        await browser.waitForAngular();
        await element(by.cssContainingText('td', 'test-client')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Edit')).click();
        await browser.waitForAngular();
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();

    });

    it('should be able to delete OAuth Client', async () => {
        await element(by.cssContainingText('td', 'test-client')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Delete')).click();
        await browser.waitForAngular();
        await element(by.cssContainingText('jhi-oauth-client-delete-dialog button.btn-danger', 'Delete')).click();
    });

    afterAll(async () => {
        await browser.waitForAngular();
        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignOut();
        browser.sleep(1000);
    });
});
