import { browser, element, by, $ } from 'protractor';

describe('Subject e2e test', () => {

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

    beforeEach( () => {
        browser.waitForAngular();
    });

    it('should load Subjects', () => {
        entityMenu.click();
        element.all(by.css('[routerLink="subject"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.subject.home.title/;
            element.all(by.css('h4 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create Subject dialog', function() {
        element(by.css('button.create-subject')).click().then(() => {
            const expectVal = /managementPortalApp.subject.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create new subject', () => {
        element(by.cssContainingText('button.btn-primary', 'Create a new Subject')).click().then(() => {
            element(by.name('externalId')).sendKeys('test-subject1');
            element(by.name('project')).sendKeys('radar');

            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-subjects tbody tr')).count().then(function(count) {
                    expect(count).toEqual(5);
                });
            });
        });
    });

    it('should be able to edit a source', () => {
        element.all(by.cssContainingText('jhi-subjects tbody tr td', 'test-subject1'))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('jhi-subjects tbody tr button', 'Edit'))
                .first().click().then(() => {
            element(by.name('externalLink')).sendKeys('www.radarcns.org');
            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-subjects tbody tr')).count().then(function(count) {
                    expect(count).toEqual(5);
                });
            });

        });
    });

    it('should be able to delete a subject without source', () => {
        element(by.cssContainingText('jhi-subjects tbody tr td', 'test-subject1'))
                .element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-subject-delete-dialog button.btn-danger', 'Delete'))
                    .click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-subjects tbody tr')).count().then(function(count) {
                    expect(count).toEqual(4);
                });
            });
        });
    });

    afterAll(function() {
        accountMenu.click();
        logout.click();
    });
});
