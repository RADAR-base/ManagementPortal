import { browser, element, by, $ } from 'protractor';

describe('Create, edit, and delete user', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const accountMenu = element(by.id('account-menu'));
    const adminMenu = element(by.id('admin-menu'));
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

    it('should load user management view', () => {
        adminMenu.click();
        element(by.css('[routerLink="user-management"]')).click();
        const expect1 = /userManagement.home.title/;
        element.all(by.css('h2 span')).first().getAttribute('jhiTranslate').then((value) => {
            expect(value).toMatch(expect1);
        });
    });

    it('should load create a new user dialog', () => {
        element(by.cssContainingText('button.btn-primary', 'Create a new user')).click().then(() => {
            const expectVal = /userManagement.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create new user with roles', () => {
        element(by.cssContainingText('button.btn-primary', 'Create a new user')).click().then(() => {
            element(by.name('login')).sendKeys('test-user-radar');
            element(by.name('firstName')).sendKeys('Bob');
            element(by.name('lastName')).sendKeys('Rob');
            element(by.name('email')).sendKeys('rob@radarcns.org');
            element(by.name('authority')).sendKeys('ROLE_PROJECT_ADMIN');
            element(by.name('project')).sendKeys('radar');
            element(by.name('addRole')).click();

            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-user-mgmt tbody tr')).count().then(function(count) {
                    expect(count).toEqual(8);
                });
            });
        });
    });

    it('should be able to create new system admin user', () => {
        element(by.cssContainingText('button.btn-primary', 'Create an admin user')).click().then(() => {
            element(by.name('login')).sendKeys('test-sys-admin');
            element(by.name('firstName')).sendKeys('Alice');
            element(by.name('lastName')).sendKeys('Bob');
            element(by.name('email')).sendKeys('alice@radarcns.org');

            element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-user-mgmt tbody tr')).count().then(function(count) {
                    expect(count).toEqual(9);
                });
            });
        });
    });

    it('should be able to edit a user with roles', () => {
        element.all(by.cssContainingText('jhi-user-mgmt tbody tr td', 'test-user-radar'))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('jhi-user-mgmt tbody tr button', 'Edit'))
                .first().click().then(() => {
                    element(by.name('lastName')).sendKeys('Robert');
                    element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
                        browser.waitForAngular();
                        element.all(by.css('jhi-user-mgmt tbody tr')).count().then(function(count) {
                            expect(count).toEqual(9);
                        });
                    });

        });
    });

    it('should be able to delete a user with roles', () => {
        element(by.cssContainingText('jhi-user-mgmt tbody tr td', 'test-user-radar'))
                .element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-user-mgmt-delete-dialog button.btn-danger', 'Delete'))
                    .click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-user-mgmt tbody tr')).count().then(function(count) {
                    expect(count).toEqual(8);
                });
            });
        });
    });

    it('should be able to delete a sys admin user', () => {
        element(by.cssContainingText('jhi-user-mgmt tbody tr td', 'test-sys-admin'))
                .element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-user-mgmt-delete-dialog button.btn-danger', 'Delete'))
                    .click().then(() => {
                browser.waitForAngular();
                element.all(by.css('jhi-user-mgmt tbody tr')).count().then(function(count) {
                    expect(count).toEqual(7);
                });
            });
        });
    });

    afterAll(function() {
        accountMenu.click();
        logout.click();
    });
});
