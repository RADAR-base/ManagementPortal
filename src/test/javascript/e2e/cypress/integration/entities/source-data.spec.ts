import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('SourceData e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load SourceData', () => {
        navBarPage.clickOnEntityMenu();
        cy.get('[routerLink="source-data"]').first().click();

        const pageTitle = cy.get('h2 span').first();
        pageTitle.should('have.text', 'Source Data');
    });

    it('should load create SourceData dialog', () => {
        cy.get('button.create-source-data').click();

        const modalTitle = cy.get('h4.modal-title').first();
        modalTitle.should('have.text', 'Create or edit a Source Data');

        cy.get('button.close').click();
    });

    it('should be able to create SourceData', () => {
        cy.get('button.create-source-data').click();
        cy.wait(3000);

        cy.get('#field_sourceDataType').type('TEST-TYPE');
        cy.get('#field_sourceDataName').type('TEST-SENSOR');
        cy.wait(3000);
        cy.get('jhi-source-data-dialog').contains('Save').click();

    });

    it('should be able to edit SourceData', () => {
        cy.wait(3000);
        cy.get('td').contains('TEST-SENSOR').parents('tr')
            .find('button').contains('Edit').click();

        cy.wait(3000);
        cy.get('button.btn-primary').contains('Save').click();
    });

    it('should be able to delete SourceData', () => {
        cy.get('td').contains('TEST-SENSOR').parents('tr')
            .find('button').contains('Delete').click();

        cy.get('jhi-source-data-delete-dialog button.btn-danger')
            .contains('Delete').click();
    });
});
