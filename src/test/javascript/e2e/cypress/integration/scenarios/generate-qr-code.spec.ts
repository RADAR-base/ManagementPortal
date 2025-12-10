import { login } from '../util/login';

describe('Project view: Generate QR code', () => {
    before(() => {
        cy.wait(1000);
    });

    beforeEach(() => {
        cy.visit('/');
        login();
        cy.wait(1000);
        cy.contains('jhi-home .card-title', 'main', { timeout: 10000 }).click();
        cy.contains('jhi-projects table tbody td a', 'radar', { timeout: 10000 }).click();
    });

    it('should open pair app dialog', () => {
        cy.contains('button', 'Pair App').first().click();
        cy.get('[name=pairForm]').find('h4').first()
            .should('have.text', 'Pair an application');
        cy.get('#field_clientApp').select('pRMT');
        cy.get('button').contains('Generate QR code').click();
        cy.get('qr-code').should('have.length', 1);
        cy.get('button.close').click();
    });
});
