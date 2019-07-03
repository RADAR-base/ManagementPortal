import { browser, element, by, $ } from 'protractor';

describe('SourceData e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const entityMenu = element(by.id('entity-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    beforeAll(() => {
        return browser.get('#')
                .then(() => accountMenu.click())
                .then(() => login.click())
                .then(() => username.sendKeys('admin'))
                .then(() => password.sendKeys('admin'))
                .then(() => element(by.css('button[type=submit]')).click())
                .then(() => browser.waitForAngular())
    });

    it('should load SourceData', () => {
        entityMenu.click();
        element.all(by.css('[routerLink="source-data"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.sourceData.home.title/;
            element.all(by.css('h2 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create SourceData dialog', function() {
        element(by.css('button.create-source-data')).click().then(() => {
            const expectVal = /managementPortalApp.sourceData.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create SourceData', function() {
        element(by.css('button.create-source-data')).click().then(() => {
            element(by.id('field_sourceDataType')).sendKeys('TEST-TYPE');
            element(by.id('field_sourceDataName')).sendKeys('TEST-SENSOR');
            element(by.css('jhi-source-data-dialog')).element(by.buttonText('Save')).click();
        });
    });

    it('should be able to edit SourceData', () => {
        browser.waitForAngular();
        element(by.cssContainingText('td', 'TEST-SENSOR')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Edit')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('button.btn-primary', 'Save')).click();
        });
    });

    it('should be able to delete SourceData', () => {
        element(by.cssContainingText('td', 'TEST-SENSOR')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-source-data-delete-dialog button.btn-danger', 'Delete')).click();
        });
    });

    afterAll(function() {
        accountMenu.click();
        logout.click();
    });
});
