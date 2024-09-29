import {login} from '../util/login';

describe('Group e2e test', () => {
    beforeEach(() => {
        login();
        cy.contains('jhi-home .card-title', 'main').click();
        cy.contains('jhi-projects table tbody td a', 'radar').click();
        cy.contains('jhi-project-detail ul.nav-tabs .nav-item', 'Groups').click();
    });

    it('should load Groups', () => {
        cy.get('jhi-groups .group-row').should('have.length', 2);
    });

    it('should load create Group dialog', () => {
        cy.get('jhi-groups button.create-group').click();
        cy.get('jhi-group-dialog h4.modal-title').first().should('have.text', 'Create or edit a Group');
        cy.get('jhi-group-dialog button.close').click();
    });

    it('should be able to create new group', () => {
        cy.get('jhi-groups button.create-group').click();
        cy.get('jhi-group-dialog input[name=name]').type('Test Group C');
        cy.contains('jhi-group-dialog button.btn-primary', 'Save').click();
        cy.get('jhi-groups .group-row').should('have.length', 3);
    });

    it('should be able to delete a group', () => {
        cy.contains('jhi-project-detail ul.nav-tabs .nav-item', 'Groups').click();
        cy.contains('jhi-groups .group-row', 'Test Group C').contains('button', 'Delete').click();
        cy.get('jhi-groups .group-row').should('have.length', 2);
    });
});
