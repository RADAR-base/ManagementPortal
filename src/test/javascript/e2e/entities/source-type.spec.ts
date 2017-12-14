import { browser, element, by, $ } from 'protractor';

describe('SourceType e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const entityMenu = element(by.id('entity-menu'));
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

    it('should load SourceTypes', () => {
        entityMenu.click();
        element.all(by.css('[routerLink="source-type"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.sourceType.home.title/;
            element.all(by.css('h2 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create SourceType dialog', function () {
        element(by.css('button.create-source-type')).click().then(() => {
            const expectVal = /managementPortalApp.sourceType.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create SourceType', function () {
        element(by.css('button.create-source-type')).click().then(() => {
            element(by.id('field_producer')).sendKeys('test-producer');
            element(by.id('field_model')).sendKeys('test-model');
            element(by.id('field_catalogVersion')).sendKeys('v1');
            // select first option in the source type scope dropdown
            element(by.id('field_sourceTypeScope')).all(by.tagName('option')).then(function(options){
              options[0].click();
            });
            element(by.css('jhi-source-type-dialog')).element(by.buttonText('Save')).click().then(() => {
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/A new Source Type is created with identifier test-producer test-model v1/);
                });
            });
        });
    });
    
    it('should be able to edit SourceType', () => {
        element(by.cssContainingText('td', 'test-producer')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Edit')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/A Source Type is updated with identifier test-producer test-model v1/);
                });
            });
        });
    });

    it('should be able to delete SourceType', () => {
        element(by.cssContainingText('td', 'test-producer')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-source-type-delete-dialog button.btn-danger', 'Delete')).click().then(() =>{
                // there should be a notification popup
                element(by.css('.alert-success')).element(by.css('pre')).getText().then((value) => {
                    expect(value).toMatch(/A Source Type is deleted with identifier test-producer test-model v1/);
                });
            });
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
