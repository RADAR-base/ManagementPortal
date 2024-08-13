import { login } from '../util/login';

describe('account', () => {
    before(() => {
        login();
    });

    it('should login successfully with admin account', () => {
        cy.get('.alert-success').first()
            .should('have.text', 'You are logged in as user "admin".');
    });

    it('should be able to update settings', () => {
        cy.get('#account-menu').first()
            .should('have.text', 'Account');
    });
});
