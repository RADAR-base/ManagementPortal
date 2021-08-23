import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('SourceType e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load SourceTypes', () => {
        navBarPage.clickOnEntityMenu();
        navBarPage.clickOnEntity('source-type');

        cy.get('h2 span').first().should('have.text', 'Source Types');
    });

    it('should load create SourceType dialog', () => {
        cy.get('button.create-source-type').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Source Type');

        cy.get('button.close').click();
    });

    it('should be able to create SourceType', () => {
        cy.get('button.create-source-type').click();
        cy.get('#field_producer').type('test-producer');
        cy.get('#field_model').type('test-model');
        cy.get('#field_catalogVersion').type('v1');
        // select first option in the source type scope dropdown
        cy.get('#field_sourceTypeScope').select('ACTIVE');
        cy.get('jhi-source-type-dialog button').contains('Save').click();
    });

    it('should be able to edit SourceType', () => {
        cy.wait(500);
        cy.get('td').contains('test-producer').parents('tr')
            .find('button').contains('Edit').click();
        cy.get('button.btn-primary').contains('Save').click();
    });

    it('should be able to delete SourceType', () => {
        cy.get('td').contains('test-producer').parents('tr')
            .find('button').contains('Delete').click();
        cy.get('jhi-source-type-delete-dialog button.btn-danger')
            .contains('Delete').click();
    });

});
