import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe.skip('Project view: Create a subject, assign sources, discontinue and delete a subject.', () => {
    const sourceName = 'test-source-2';
    const externalId = 'test-id';

    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load project view', () => {
        navBarPage.clickOnProjectMenu();
        cy.get('a').contains('radar').first().click();
    });

    it('should be able to create a subject', () => {
        cy.get('jhi-subjects h4 button.btn-primary')
            .contains('Create a new Subject').click();
        cy.get('[name=externalId]').type(externalId);
        cy.get('button.btn-primary').contains('Save').click();

        cy.get('jhi-subjects tbody tr').should('have.length', 4);
    });

    it('should be able to create a source', () => {
        cy.get('li').contains('Sources').click();
        cy.get('button.create-source').click();
        cy.get('#field_sourceName').type(sourceName);
        cy.get('#field_expectedSourceName').type('AABBCC');
        cy.get('#field_sourceType').select('App');

        cy.get('.modal-footer button.btn-primary').click();

        cy.get('jhi-sources tbody tr').should('have.length', 3);
    });

    it('should be able to assign a source', () => {
        cy.get('li').contains('Subjects').click();
        // find a row which contains the external-id entered
        cy.get('jhi-subjects tbody tr td').contains(externalId).parents('tr')
            .find('button')
            .contains('Pair Sources').click();

        // first table lists assigned sources, this should be empty
        cy.get('jhi-source-assigner tbody').first()
            .find('tr').should('have.length', 0);
        // second table lists available sources, should have two elements
        cy.get('jhi-source-assigner tbody').last()
            .find('tr').should('have.length', 2);

        cy.get('jhi-source-assigner tbody').last()
            .find('tr').contains(sourceName)
            .find('button').contains('Add').click();
        // available source should be moved to first table
        cy.get('jhi-source-assigner tbody').first()
            .find('tr').should('have.length', 1);
        cy.get('jhi-source-assigner tbody').last()
            .find('tr').should('have.length', 1);

        cy.get('button').contains('Save').click();
        // check that we have exactly one cell in the subjects table containing the sourceName
        cy.get('jhi-subjects td').contains(sourceName).should('have.length', 1);
    });

    it('should show the source as assigned', () => {
        cy.get('li').contains('Sources').click();
        cy.get('a').contains(sourceName).parents('tr')
            .find('.badge-success').contains('Assigned')
            .should('have.length', 1);
    });

    it('should be able to discontinue a subject', () => {
        cy.get('li').contains('Subjects').click();
        // find a row which contains a uuid
        cy.get('jhi-subjects tbody tr td').contains(externalId).parents('tr')
            .find('button').contains('Discontinue').click();
        cy.get('h4.modal-title')
            .should('have.text', 'Confirm discontinue operation');

        cy.get('[name=deleteForm]')
            .find('button').contains('Discontinue').click();
    });

    it('should show the source as unassigned', () => {
        cy.get('li').contains('Sources').click();
        cy.get('a').contains(sourceName).parents('tr')
            .find('.badge-danger').contains('Unassigned')
            .should('have.length', 1);
    });

    it('should be able to delete a subject', () => {
        cy.get('li').contains('Subjects').click();
        cy.get('jhi-subjects tbody tr td').contains(externalId).parents('tr')
            .find('button').contains('Delete')
            .click();

        cy.get('h4.modal-title').should('have.text', 'Confirm delete operation');
        cy.get('.modal-footer button.btn-danger').click();
        cy.get('jhi-subjects tbody tr').should('have.length', 3);
    });
});
