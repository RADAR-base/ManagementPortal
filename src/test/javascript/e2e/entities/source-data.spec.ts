import { $, browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('SourceData e2e test', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));

    beforeAll(async () => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        await browser.waitForAngular();
    });

    beforeEach(async () => {
        browser.sleep(1000);
    });

    it('should load SourceData', async () => {
        await navBarPage.clickOnEntityMenu();
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
});
