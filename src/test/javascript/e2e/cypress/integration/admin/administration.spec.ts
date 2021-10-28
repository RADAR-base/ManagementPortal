import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('administration', () => {
    before(() => {
        cy.visit('./');
    });

    beforeEach(() => {
        login();
        cy.wait(100);
        navBarPage.clickOnAdminMenu();
    });

    it('should load user management', () => {
        navBarPage.clickOnEntity('user-management');
        cy.get('h2 span').first().should('have.text', 'Users');
    });

    it('should load metrics', () => {
        navBarPage.clickOnEntity('jhi-metrics');
        cy.get('h2 span').first().should('have.text', 'Application Metrics');
    });

    it('should load health', () => {
        navBarPage.clickOnEntity('jhi-health');
        cy.get('h2 span').first().should('have.text', 'Health Checks');
    });

    it('should load audits', () => {
        navBarPage.clickOnEntity('audits');
        cy.get('h2').first().should('have.text', 'Audits');
    });

    it('should load logs', () => {
        navBarPage.clickOnEntity('logs');
        cy.get('h2').first().should('have.text', 'Logs');
    });

});
