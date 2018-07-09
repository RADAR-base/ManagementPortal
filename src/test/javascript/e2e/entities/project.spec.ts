import { browser, element, by, $ } from 'protractor';

describe('Project e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const projectMenu = element(by.id('projects-menu'));
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

    it('should load Projects', () => {
        adminMenu.click();
        element.all(by.css('[routerLink="project"]')).first().click().then(() => {
            const expectVal = /managementPortalApp.project.home.title/;
            element.all(by.css('h2 span')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });
        });
    });

    it('should load create Project dialog', () => {
        element(by.css('button.create-project')).click().then(() => {
            const expectVal = /managementPortalApp.project.home.createOrEditLabel/;
            element.all(by.css('h4.modal-title')).first().getAttribute('jhiTranslate').then((value) => {
                expect(value).toMatch(expectVal);
            });

            element(by.css('button.close')).click();
        });
    });

    it('should be able to create Project', () => {
        element(by.css('button.create-project')).click().then(() => {
            element(by.id('field_projectName')).sendKeys('test-project');
            element(by.id('field_humanReadableProjectName')).sendKeys('Test project');
            element(by.id('field_description')).sendKeys('Best test project in the world');
            element(by.id('field_location')).sendKeys('in-memory');
            element(by.cssContainingText('jhi-project-dialog button.btn-primary', 'Save')).click();
        });
    });

    it('should be able to edit Project', () => {
        browser.waitForAngular();
        element(by.cssContainingText('td', 'test-project')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Edit')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('button.btn-primary', 'Save')).click();
        });
    });

    it('should be able to delete Project', () => {
        element(by.cssContainingText('td', 'test-project')).element(by.xpath('ancestor::tr'))
                .element(by.cssContainingText('button', 'Delete')).click().then(() => {
            browser.waitForAngular();
            element(by.cssContainingText('jhi-project-delete-dialog button.btn-danger', 'Delete')).click();
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
