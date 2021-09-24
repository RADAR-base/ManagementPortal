import * as navBarPage from '../util/nav-bar';

describe('account', () => {
    before(() => {
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should fail to login with bad password', () => {
        cy.get('h1').first()
            .should('have.text', 'Welcome to RADAR Management Portal');

        navBarPage.clickOnAccountMenu();
        navBarPage.clickOnSignIn();

        cy.get('#username').type('admin');
        cy.get('#password').type('foo');
        cy.get('button[type=submit]').click();

        let msg = 'Failed to sign in! Please check your credentials and try again.';
        cy.get('.alert-danger').first().should('have.text', msg);
        cy.get('#username').clear();
        cy.get('#password').clear();
    });

    it('should login successfully with admin account', () => {
        cy.get('.modal-content h1').first().should('have.text', 'Sign in');

        cy.get('#username').type('admin');
        cy.get('#password').type('admin');
        cy.get('button[type=submit]').click();

        cy.wait(1000);

        cy.get('.alert-success span').should('exist');
    });

    it('should be able to update settings', () => {
        navBarPage.clickOnAccountMenu();
        cy.get('[routerLink="settings"]').click();

        cy.get('h2').first()
            .should('have.text', 'User settings for [admin]');

        cy.get('button[type=submit]').click();

        cy.get('.alert-success').first()
            .should('have.text', 'Settings saved!');
    });
});
