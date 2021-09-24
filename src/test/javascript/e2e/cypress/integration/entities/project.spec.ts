import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load Projects', () => {
        navBarPage.clickOnAdminMenu();
        navBarPage.clickOnEntity('project');

        cy.get('h2 span').first().should('have.text', 'Projects');
    });

    it('should load create Project dialog', () => {
        cy.get('button.create-project').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Project');

        cy.get('button.close').click();
    });

    it('should be able to create Project', () => {
        cy.get('button.create-project').click();
        cy.wait(3000);
        cy.get('#field_projectName').type('test-project');
        cy.get('#field_humanReadableProjectName').type('Test project');
        cy.get('#field_description').type('Best test project in the world');
        cy.get('#field_location').type('in-memory');
        cy.wait(3000);
        cy.get('jhi-project-dialog button.btn-primary').contains('Save').click();
    });

    it('should be able to edit Project', () => {
        cy.wait(3000);
        cy.get('td').contains('test-project').parents('tr')
            .find('button').contains('Edit').click();
        cy.get('button.btn-primary').contains('Save').click();
    });

    it('should be able to delete Project', () => {
        cy.get('td').contains('test-project').parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-project-delete-dialog button.btn-danger')
            .contains('Delete').click();
    });
});
