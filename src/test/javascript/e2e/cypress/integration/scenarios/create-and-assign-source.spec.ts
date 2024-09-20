import {login} from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project View: Create, assign, unassign and delete source', () => {
    const sourceName = 'test-source-1';

    beforeEach(() => {
        login();
        navBarPage.loadProjectView()
    });

    // TODO:
    // Clarify the process of source deletion after the source was assigned

    // it('should be able to create a source', () => {
    //     cy.get('li').contains('Sources').click();
    //     cy.get('button.create-source').click();
    //     cy.get('#field_sourceName').type(sourceName);
    //     cy.get('#field_expectedSourceName').type('AABBCC');
    //     cy.get('#field_sourceType').select('App');

    //     cy.get('button.btn-primary').contains('Save').click();
    //     cy.get('jhi-sources tbody tr').should('have.length', 2);
    // });

    // it('should be able to assign a source', () => {
    //     cy.get('li').contains('Subjects').click();
    //     cy.get('jhi-subjects tbody tr td').contains('sub-2').parents('tr')
    //         .find('button').contains('Pair Sources')
    //         .first().click();
    //     // first table lists assigned sources, this should be empty
    //     cy.get('jhi-source-assigner tbody').first()
    //         .find('tr').should('have.length', 0);
    //     // second table lists available sources, should have one element
    //     cy.get('jhi-source-assigner tbody').last()
    //         .find('tr').should('have.length', 1);

    //     cy.wait(500);
    //     cy.get('button').contains('Add').click();
    //     // available source should be moved to first table
    //     cy.get('jhi-source-assigner tbody').first()
    //         .find('tr').should('have.length', 1);
    //     cy.get('jhi-source-assigner tbody').last()
    //         .find('tr').should('have.length', 0);

    //     cy.get('button').contains('Save').click();
    //     // check that we have exactly one cell in the subjects table containing the sourceName
    //     cy.get('jhi-subjects td').contains(sourceName).should('have.length', 1);
    // });

    // it('should show the source as assigned', () => {
    //     cy.get('li').contains('Sources').click();
    //     cy.get('a').contains(sourceName).parents('tr')
    //         .find('.badge-success').contains('Assigned')
    //         .should('have.length', 1);
    // });

    // it('should not be able to delete an assigned source', () => {
    //     cy.get('a').contains(sourceName).parents('tr')
    //         .find('button').contains('Delete').first()
    //         .click();
    //     cy.get('.modal-footer button').contains('Delete').click();
    //     // if the delete succeeded the dialog will be disappeared and no Cancel button will be here anymore
    //     cy.get('button').contains('Cancel').click();
    // });

    // it('should be able to unassign a source', () => {
    //     cy.get('li').contains('Subjects').click();
    //     cy.wait(500);
    //     cy.get('jhi-subjects td a').contains(sourceName).parents('tr')
    //         .find('button').contains('Pair Sources').first().click();
    //     cy.get('button').contains('Remove').click();
    //     // source should be moved back to available sources table

    //     cy.get('jhi-source-assigner tbody').first()
    //         .find('tr').should('have.length', 0);
    //     cy.get('jhi-source-assigner tbody').last()
    //         .find('tr').should('have.length', 1);

    //     cy.get('button').contains('Save').click();
    //     // check that we have no cells in the subjects table containing the sourceName
    //     cy.get('jhi-subjects td').contains(sourceName).should('have.length', 0);
    // });

    // it('should not be able to delete a source used by subject', () => {
    //     cy.get('li').contains('Sources').click();
    //     cy.get('a').contains(sourceName).parents('tr')
    //         .find('button').contains('Delete')
    //         .click();

    //     cy.get('.modal-footer button').contains('Delete').click();

    //     cy.get('button').contains('Cancel').click();
    // });
});
