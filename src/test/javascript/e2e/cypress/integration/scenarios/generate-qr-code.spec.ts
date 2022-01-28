import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project view: Generate QR code', () => {
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
        cy.get('.status-header').invoke('text').should('match', /RADAR/i);
        // expect 3 subjects in this table
        cy.get('jhi-subjects tbody tr').should('have.length', 3);
    });

    it('should open pair app dialog', () => {
        cy.get('button:not(:disabled)').contains('Pair App').first().click();
        cy.get('[name=pairForm]').find('h4').first()
            .should('have.text', 'Pair an application');
    });

    it('should be able to create a qr code', () => {
        cy.get('#field_clientApp').select('pRMT');
        cy.get('button').contains('Generate QR code').click();
        cy.get('qrcode').should('have.length', 1);
        cy.get('button.close').click();
    });

});
