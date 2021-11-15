import { login } from '../util/login';

describe('Project view: Generate QR code', () => {
    beforeEach(() => {
        login();
        cy.contains('jhi-home .card-title', 'radar').click();
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
