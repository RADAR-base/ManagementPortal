import * as navBarPage from '../util/nav-bar';
import { login } from "../util/login";

describe('account', () => {

    it('should show welcome page when not logged in', () => {
        cy.visit('/', {
            timeout: 60000,
            failOnStatusCode: false
        });
        cy.get('h1', { timeout: 10000 }).first()
            .should('have.text', 'Welcome to RADAR Management Portal');
    });

    it('should login successfully with admin account', () => {
        login('admin', 'admin');

        // Wait for page to be ready after login
        cy.wait(3000);

        // Verify we're logged in by checking account menu shows user info
        cy.get('h1', { timeout: 10000 }).should('exist');
        navBarPage.clickOnAccountMenu();
        cy.get('#logout', { timeout: 5000 }).should('exist');
    });

    it('should be able to update settings', () => {
        login('admin', 'admin');

        // Wait for page to be ready after login
        cy.wait(3000);
        cy.get('h1', { timeout: 10000 }).should('exist');

        navBarPage.clickOnAccountMenu();
        navBarPage.clickOnEntity('settings');

        cy.get('h2', { timeout: 10000 }).first()
            .should('have.text', 'User settings for [admin]');

        cy.get('button[type=submit]').click();

        cy.get('.alert-success', { timeout: 10000 }).first()
            .should('have.text', 'Settings saved!');
    });
});
