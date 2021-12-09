import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Organization e2e test', () => {
    beforeEach(() => {
        login();
        navBarPage.clickOnAdminMenu();
        navBarPage.clickOnEntity('organization');
    });

    it('should load Organizations', () => {
        cy.get('h2 span').first().should('have.text', 'Organizations');
    });

    it('should load create Organization dialog', () => {
        cy.get('button.create-organization').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Organization');

        cy.get('button.close').click();
    });

    it('should be able to create Organization', () => {
        cy.get('button.create-organization').click();
        cy.get('#field_organizationName').type('test-organization');
        cy.get('#field_description').type('Best test organization in the world');
        cy.get('#field_location').type('in-memory');
        cy.contains('jhi-organization-dialog button.btn-primary', 'Save').click();
    });

    it('should be able to edit Organization', () => {
        cy.contains('tr', 'test-organization')
            .contains('button', 'Edit').click();
        cy.contains('button.btn-primary', 'Save').click();
    });

    it('should be able to delete Organization', () => {
        cy.contains('tr', 'test-organization')
            .contains('button', 'Delete').click();
        cy.contains('jhi-organization-delete-dialog button.btn-danger', 'Delete').click();
    });
});
