import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Source e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load Sources', () => {
        navBarPage.clickOnEntityMenu();
        navBarPage.clickOnEntity('source');

        cy.get('h4 span').first().should('have.text', 'Sources');
    });

    it('should load create Source dialog', () => {
        cy.get('button.create-source').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Source');

        cy.get('button.close').click();
    });

    it('should be able to create new source', () => {
        cy.get('button.btn-primary').contains('Create a new Source').click();
        cy.wait(500);
        cy.get('[name=sourceName]').type('test-source1');
        cy.get('[name=expectedSourceName]').type('A007C');
        cy.get('[name=project]').select('radar');
        cy.get('[name=sourceType]').select('Empatica_E4_v1');

        cy.get('button.btn-primary').contains('Save').click();
        cy.get('jhi-source tbody tr').should('have.length', 2);
    });

    it('should be able to edit a source', () => {
        cy.wait(500);
        cy.get('jhi-source tbody tr td').contains('test-source1').parents('tr')
            .find('button').contains('Edit')
            .first().click();
        cy.get('[name=expectedSourceName]').type('A007C9');
        cy.get('button.btn-primary').contains('Save').click();
        cy.get('jhi-source tbody tr').should('have.length', 2);
    });

    it('should be able to delete an unassigned source', () => {
        cy.get('jhi-source tbody tr td').contains('test-source1').parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-source-delete-dialog button.btn-danger').contains('Delete')
            .click();
        cy.get('jhi-source tbody tr').should('have.length', 1);
    });

    // Source creation and deletion already covered in scenarios/create-and-assign-source.spec.ts
});
