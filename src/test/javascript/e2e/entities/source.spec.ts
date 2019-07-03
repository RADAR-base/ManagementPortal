import { browser, element, by, $ } from 'protractor';

describe('Source e2e test', () => {

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

    it('should load Sources', () => {
        entityMenu.click();
        element.all(by.css('[routerLink="source"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.source.home.title/;
            element.all(by.css('h4 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create Source dialog', function() {
        element(by.css('button.create-source')).click().then(() => {
            const expectVal = /managementPortalApp.source.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create new source', () => {
        element(by.cssContainingText('button.btn-primary', 'Create a new Source')).click().then(() => {
            element(by.name('sourceName')).sendKeys('test-source1');
            element(by.name('expectedSourceName')).sendKeys('A007C');
            element(by.name('project')).sendKeys('radar');
            element(by.name('sourceType')).sendKeys('Empatica_E4_v1');

            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-source tbody tr')).count().then(function(count) {
                    expect(count).toEqual(2);
                });
            });
        });
    });

    it('should be able to edit a source', () => {
        element.all(by.cssContainingText('jhi-source tbody tr td', 'test-source1'))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('jhi-source tbody tr button', 'Edit'))
                .first().click().then(() => {
            element(by.name('expectedSourceName')).sendKeys('A007C9');
            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-source tbody tr')).count().then(function(count) {
                    expect(count).toEqual(2);
                });
            });

        });
    });

    it('should be able to delete an unassigned source', () => {
        element(by.cssContainingText('jhi-source tbody tr td', 'test-source1'))
                .element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-source-delete-dialog button.btn-danger', 'Delete'))
                    .click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-source tbody tr')).count().then(function(count) {
                    expect(count).toEqual(1);
                });
            });
        });
    });

    // Source creation and deletion already covered in scenarios/create-and-assign-source.spec.ts

    afterAll(function() {
        accountMenu.click();
        logout.click();
    });
});
