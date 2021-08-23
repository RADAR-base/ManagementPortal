import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Create, edit, and delete user', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load user management view', () => {
        navBarPage.clickOnAdminMenu();
        navBarPage.clickOnEntity('user-management');

        cy.get('h2 span').first().should('have.text', 'Users');
    });

    it('should load create a new user dialog', () => {
        cy.get('button.btn-primary').contains('Create a new user').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a user');

        cy.get('button.close').click();
    });

    it('should be able to create new user with roles', () => {
        cy.get('button.btn-primary').contains('Create a new user').click();
        cy.get('[name=login]').type('test-user-radar');
        cy.get('[name=firstName]').type('Bob');
        cy.get('[name=lastName]').type('Rob');
        cy.get('[name=email]').type('rob@radarbase.org');
        cy.get('[name=authority]').select('ROLE_PROJECT_ADMIN');
        cy.get('[name=project]').select('radar');
        cy.get('[name=addRole]').click();

        cy.get('button.btn-primary').contains('Save').click();

        cy.get('jhi-user-mgmt tbody tr').should('have.length', 4);
    });

    it('should be able to create new system admin user', () => {
        cy.get('button.btn-primary').contains('Create an admin user').click();
        cy.get('[name=login]').type('test-sys-admin');
        cy.get('[name=firstName]').type('Alice');
        cy.get('[name=lastName]').type('Bob');
        cy.get('[name=email]').type('alice@radarbase.org');

        cy.get('button.btn-primary').contains('Save').click();
        cy.get('jhi-user-mgmt tbody tr').should('have.length', 5);
    });

    it('should be able to edit a user with roles', () => {
        cy.get('jhi-user-mgmt tbody tr td').contains('test-user-radar')
            .parents('tr')
            .find('button').contains('Edit')
            .first().click();
        cy.get('[name=lastName]').type('Robert');
        cy.get('button.btn-primary').contains('Save').click();

        cy.get('jhi-user-mgmt tbody tr').should('have.length', 5);
    });

    it('should be able to delete a user with roles', () => {
        cy.get('jhi-user-mgmt tbody tr td').contains('test-user-radar')
            .parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-user-mgmt-delete-dialog button.btn-danger')
            .contains('Delete')
            .click();

        cy.get('jhi-user-mgmt tbody tr').should('have.length', 4);
    });

    it('should be able to delete a sys admin user', () => {
        cy.get('jhi-user-mgmt tbody tr td').contains('test-sys-admin')
            .parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-user-mgmt-delete-dialog button.btn-danger').contains('Delete')
            .click();

        cy.get('jhi-user-mgmt tbody tr').should('have.length', 3);
    });
});
