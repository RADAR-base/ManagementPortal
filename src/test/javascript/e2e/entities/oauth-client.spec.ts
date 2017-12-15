import { browser, element, by, $ } from 'protractor';

describe('OAuth Clients e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const adminMenu = element(by.id('admin-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    beforeAll(() => {
        browser.get('#');

        accountMenu.click();
        login.click();

        username.sendKeys('admin');
        password.sendKeys('admin');
        element(by.css('button[type=submit]')).click();
        browser.waitForAngular();
    });

    it('should load OAuth clients', () => {
        adminMenu.click();
        element.all(by.css('[routerLink="oauth-client"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.oauthClient.home.title/;
            element.all(by.css('h4 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create OAuth Client dialog', () => {
        element(by.css('jhi-oauth-client h4 button.btn-primary')).click().then(() => {
            const expectVal = /managementPortalApp.oauthClient.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should disable edit and delete buttons for protected clients', () => {
        // find the table row that contains the protected badge, and assert it contains zero enabled buttons
        element(by.cssContainingText('span.badge-info', 'protected: true')).element(by.xpath('ancestor::tr'))
                .all(by.css('button')).filter((button) => button.isEnabled()).count().then((count) =>
            expect(count).toEqual(2));// show more, show less buttons are enabled
    });

    it('should be able to create OAuth Client', () => {
        element(by.css('jhi-oauth-client h4 button.btn-primary')).click().then(() => {
            element(by.id('id')).sendKeys('test-client');
            element(by.cssContainingText('button', 'Random')).click().then(() => {
                // check if there is something put in the secret field
                element(by.id('secret')).getAttribute('value').then((value) => expect(value.length).toBeGreaterThan(0));
            });
            element(by.id('scope')).sendKeys('SUBJECT.READ');
            element(by.id('resourceIds')).sendKeys('res_ManagementPortal');
            element(by.cssContainingText('label.form-check-label', 'refresh_token')).element(by.css('input')).click();
            element(by.cssContainingText('label.form-check-label', 'password')).element(by.css('input')).click();
            element(by.id('accessTokenValidity')).clear();
            element(by.id('accessTokenValidity')).sendKeys('3600');
            element(by.id('refreshTokenValidity')).clear();
            element(by.id('refreshTokenValidity')).sendKeys('7200');
            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() =>{
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/A new OAuth client is created with client id test-client/);
                });
            });
        });
    });

    it('should be able to edit OAuth Client', () => {
        element(by.cssContainingText('td', 'test-client')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Edit')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() =>{
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/An OAuth client is updated with client id test-client/);
                });
            });
        });
    });

    it('should be able to delete OAuth Client', () => {
        element(by.cssContainingText('td', 'test-client')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-oauth-client-delete-dialog button.btn-danger', 'Delete')).click().then(() =>{
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/An OAuth client is deleted with client id test-client/);
                });
            });
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
