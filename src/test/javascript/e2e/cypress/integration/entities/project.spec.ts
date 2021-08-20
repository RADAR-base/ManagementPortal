import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load Projects', async() => {
        await navBarPage.clickOnAdminMenu();
        await element.all(by.css('[routerLink="project"]')).first().click();
        const expectVal = /managementPortalApp.project.home.title/;

        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);
    });

    it('should load create Project dialog', async() => {
        await element(by.css('button.create-project')).click();

        const expectVal = /managementPortalApp.project.home.createOrEditLabel/;
        const modalTitle = element.all(by.css('h4.modal-title')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);

        await element(by.css('button.close')).click();
    });

    it('should be able to create Project', async() => {
        await element(by.css('button.create-project')).click();
        await element(by.id('field_projectName')).sendKeys('test-project');
        await element(by.id('field_humanReadableProjectName')).sendKeys('Test project');
        await element(by.id('field_description')).sendKeys('Best test project in the world');
        await element(by.id('field_location')).sendKeys('in-memory');
        await element(by.cssContainingText('jhi-project-dialog button.btn-primary', 'Save')).click();
    });

    it('should be able to edit Project', async() => {
        await browser.waitForAngular();
        await element(by.cssContainingText('td', 'test-project')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Edit')).click();
        await browser.waitForAngular();
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
    });

    it('should be able to delete Project', async() => {
        await element(by.cssContainingText('td', 'test-project')).element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Delete')).click();
        await browser.waitForAngular();
        await element(by.cssContainingText('jhi-project-delete-dialog button.btn-danger', 'Delete')).click();

    });
});
