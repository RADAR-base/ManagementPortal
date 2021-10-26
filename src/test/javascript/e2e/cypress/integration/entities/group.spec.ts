import { login } from '../util/login';

describe('Group e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load Groups', () => {
        cy.contains('jhi-home .card-title', 'radar').click();
        cy.contains('jhi-project-detail ul.nav-tabs .nav-item', 'Groups').click();
        cy.get('jhi-groups .group-row').should('have.length', 0);
    });

    it('should load create Group dialog', () => {
        cy.get('jhi-groups button.create-group').click();
        cy.get('jhi-group-dialog h4.modal-title').first().should('have.text', 'Create or edit a Group');
        cy.get('jhi-group-dialog button.close').click();
    });

    it('should be able to create new group', () => {
        cy.get('jhi-groups button.create-group').click();
        cy.get('jhi-group-dialog input[name=name]').type('group1');
        cy.contains('jhi-group-dialog button.btn-primary', 'Save').click();
        cy.get('jhi-groups .group-row').should('have.length', 1);
    });

    // it('should be able to delete a group', () => {
    //     cy.contains('jhi-groups .group-row', 'group1').contains('button', 'Delete').click();
    //     cy.contains('jhi-group-delete-dialog button', 'Delete').click();
    //     cy.get('jhi-groups .group-row').should('have.length', 0);
    // });

});
