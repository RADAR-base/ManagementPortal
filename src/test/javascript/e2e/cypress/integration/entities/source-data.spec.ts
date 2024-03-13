import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('SourceData e2e test', () => {
    beforeEach(() => {
        login();
        navBarPage.clickOnEntityMenu();
        cy.get('[routerLink="source-data"]').first().click();
    });

    it('should load SourceData', () => {
        cy.get('h2 span').first().should('have.text', 'Source Data');
    });

    it('should load create SourceData dialog', () => {
        cy.get('button.create-source-data').click();

        const modalTitle = cy.get('h4.modal-title').first();
        modalTitle.should('have.text', 'Create or edit a Source Data');

        cy.get('button.close').click();
    });

    it('should be able to create SourceData', () => {
        cy.get('button.create-source-data').click();
        cy.get('#field_sourceDataType').type('TEST_TYPE2');
        cy.get('#field_sourceDataName').type('TEST_SENSOR2');
        cy.contains('jhi-source-data-dialog button', 'Save').click();
        cy.get('.alert-success').first()
            .should('contain', 'A new Source Data is created with identifier TEST_SENSOR');
    });

    it('should be able to edit SourceData', () => {
        cy.contains('tr', 'TEST_SENSOR2')
            .contains('button', 'Edit').click();

        cy.contains('button.btn-primary', 'Save').click();
    });

    it('should be able to delete SourceData', () => {
        cy.contains('tr', 'TEST_SENSOR2')
            .contains('button', 'Delete').click();

        cy.contains('jhi-source-data-delete-dialog button.btn-danger', 'Delete').click();
    });
});
