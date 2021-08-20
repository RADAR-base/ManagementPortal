import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('administration', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load user management', async() => {
        await navBarPage.clickOnEntity('user-management');
        const expect1 = /userManagement.home.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load metrics', async() => {
        await navBarPage.clickOnEntity('jhi-metrics');
        const expect1 = /metrics.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load health', async() => {
        await navBarPage.clickOnEntity('jhi-health');
        const expect1 = /health.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load audits', async() => {
        await navBarPage.clickOnEntity('audits');
        const expect1 = /audits.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load logs', async() => {
        await navBarPage.clickOnEntity('logs');
        const expect1 = /logs.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

});
