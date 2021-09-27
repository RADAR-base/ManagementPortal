import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('OAuth Clients e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load OAuth clients', () => {
        navBarPage.clickOnAdminMenu();
        cy.get('[routerLink="oauth-client"]').first().click();

        cy.get('h4 span').first().should('have.text', 'OAuth Clients');
    });

    it('should load create OAuth Client dialog', () => {
        cy.get('jhi-oauth-client h4 button.btn-primary').click();
        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit an OAuth Client');

        cy.get('button.close').click();
    });

    it('should disable edit and delete buttons for protected clients', () => {
        // find the table row that contains the protected badge, and assert it contains zero enabled buttons
        cy.get('span.badge-info').contains('protected: true').parents('tr')
            .find('button:not(:disabled)').should('have.length', 2);
        // show more, show less buttons are enabled
    });

    it('should be able to create OAuth Client', () => {
        cy.get('jhi-oauth-client h4 button.btn-primary').click();
        cy.wait(1000);
        cy.get('#id').type('test-client');
        cy.get('button').contains('Random').click();

        cy.get('#secret').invoke('val').should('not.be.empty');

        cy.get('#scope').type('SUBJECT.READ');
        cy.get('#resourceIds').type('res_ManagementPortal');
        cy.get('label.form-check-label').contains('refresh_token')
            .find('input').click();
        cy.get('label.form-check-label').contains('password')
            .find('input').click();
        cy.get('#accessTokenValidity').clear();
        cy.get('#accessTokenValidity').type('3600');
        cy.get('#refreshTokenValidity').clear();
        cy.get('#refreshTokenValidity').type('7200');
        cy.get('button.btn-primary').contains('Save').click();
    });

    it('should be able to edit OAuth Client', () => {
        cy.wait(1000);
        cy.get('td').contains('test-client').parents('tr')
            .find('button').contains('Edit').click();
        cy.wait(1000);
        cy.get('button.btn-primary').contains('Save').click();
    });

    it('should be able to delete OAuth Client', () => {
        cy.get('td').contains('test-client').parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-oauth-client-delete-dialog button.btn-danger')
            .contains('Delete').click();
    });
});
