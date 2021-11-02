import { login } from '../util/login';
import { clickOnEntity, clickOnEntityMenu } from "../util/nav-bar";

describe('Subject e2e test', () => {
    beforeEach(() => {
        login();
        clickOnEntityMenu();
        clickOnEntity('subject');
    });

    it('should load Subjects', () => {
        cy.get('h4 span').first().should('have.text', 'Subjects');
    });

    it('should load create Subject dialog', () => {
        cy.get('button.create-subject').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Subject');

        cy.get('button.close').click();
    });

    it('should be able to create new subject', () => {
        cy.get('button.btn-primary').contains('Create a new Subject').click();

        cy.get('[name=externalId]').type('test-subject1');
        cy.get('[name=project]').select('radar');

        cy.get('button.btn-primary').contains('Save').click();
        cy.get('jhi-subjects tbody tr').should('have.length', 5);
    });

    it('should be able to edit a subject', () => {
        cy.contains('jhi-subjects tbody tr', 'test-subject1')
            .contains('button', 'Edit')
            .first().click();
        cy.get('[name=externalLink]').type('www.radar-base.org');
        cy.get('button.btn-primary').contains('Save').click();

        cy.get('jhi-subjects tbody tr').should('have.length', 5);
    });

    it('should be able to delete a subject without source', () => {
        cy.get('jhi-subjects tbody tr td').contains('test-subject1')
            .parents('tr').find('button').contains('Delete').click();

        cy.get('jhi-subject-delete-dialog button.btn-danger')
            .contains('Delete').click();
        cy.get('jhi-subjects tbody tr').should('have.length', 4);
    });

});
