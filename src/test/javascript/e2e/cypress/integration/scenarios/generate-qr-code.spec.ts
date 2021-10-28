import { login } from '../util/login';
import { loadProjectView } from "../util/nav-bar";

describe('Project view: Generate QR code', () => {
    beforeEach(() => {
        login()
        loadProjectView();
    });

    it('should open pair app dialog', () => {
        cy.contains('button', 'Pair App').first().click();
        cy.get('[name=pairForm]').find('h4').first()
            .should('have.text', 'Pair an application');
        cy.get('#field_clientApp').select('pRMT');
        cy.get('button').contains('Generate QR code').click();
        cy.get('qrcode').should('have.length', 1);
        cy.get('button.close').click();
    });
});
