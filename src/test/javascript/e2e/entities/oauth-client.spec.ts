import { browser, element, by, $ } from 'protractor';

describe('Project e2e test', () => {

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
            expect(count).toEqual(0));
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
