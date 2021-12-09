import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project e2e test', () => {
    beforeEach(() => {
        login();
        cy.contains('jhi-home .card-title', 'main').click();
    });

    it('should load Projects', () => {
        cy.get('jhi-projects h2 span').first().should('have.text', 'Projects');
        cy.get('jhi-projects table tbody tr').should('have.length', 2);
    });

    it('should load create Project dialog', () => {
        cy.get('button.create-project').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Project');

        cy.get('button.close').click();
    });

    it('should be able to create Project', () => {
        cy.get('button.create-project').click();
        cy.get('#field_projectName').type('test-project');
        cy.get('#field_humanReadableProjectName').type('Test project');
        cy.get('#field_description').type('Best test project in the world');
        cy.get('#field_location').type('in-memory');
        cy.contains('jhi-project-dialog button.btn-primary', 'Save').click();
    });

    it('should be able to edit Project', () => {
        cy.contains('tr', 'test-project')
            .contains('button', 'Edit').click();
        cy.contains('button.btn-primary', 'Save').click();
    });

    it('should be able to delete Project', () => {
        cy.contains('tr', 'test-project')
            .contains('button', 'Delete').click();
        cy.contains('jhi-project-delete-dialog button.btn-danger', 'Delete').click();
    });
});
