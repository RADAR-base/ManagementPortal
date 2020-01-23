import { $, browser, by, element } from 'protractor';

describe('SourceData e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const entityMenu = element(by.id('entity-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    beforeAll(async () => {
        await browser.get('#');

        await accountMenu.click();
        await login.click();

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
        await browser.waitForAngular();
    });

    beforeEach(async () => {
        browser.sleep(1000);
    });

    it('should load SourceData', async () => {
        await entityMenu.click();
        await element.all(by.css('[routerLink="source-data"]')).first().click();

        const expectVal = /managementPortalApp.sourceData.home.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);
    });

    it('should load create SourceData dialog', async () => {
        await element(by.css('button.create-source-data')).click();

        const expectVal = /managementPortalApp.sourceData.home.createOrEditLabel/;
        const modalTitle = element.all(by.css('h4.modal-title')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);

        await element(by.css('button.close')).click();

    });

    it('should be able to create SourceData', async () => {
        await element(by.css('button.create-source-data')).click();

        await element(by.id('field_sourceDataType')).sendKeys('TEST-TYPE');
        await element(by.id('field_sourceDataName')).sendKeys('TEST-SENSOR');
        await element(by.css('jhi-source-data-dialog')).element(by.buttonText('Save')).click();

    });

    it('should be able to edit SourceData', async () => {
        await browser.waitForAngular();
        await element(by.cssContainingText('td', 'TEST-SENSOR')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Edit')).click();

        await browser.waitForAngular();
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
    });

    it('should be able to delete SourceData', async () => {
        await element(by.cssContainingText('td', 'TEST-SENSOR')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Delete')).click();

        await browser.waitForAngular();
        await element(by.cssContainingText('jhi-source-data-delete-dialog button.btn-danger', 'Delete')).click();

    });

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
