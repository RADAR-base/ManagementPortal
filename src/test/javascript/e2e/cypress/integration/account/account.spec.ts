import * as navBarPage from '../util/nav-bar';
import { login } from "../util/login";
import { gen } from "../../support/commands"

describe('account', () => {
    const email = gen.email()
    const password = gen.password()

    before(() => {
        console.log("Registering..")
        // cy.createUser('admin-email-here@radar-base.net', 'secret123')
      });

    beforeEach(() => {
        cy.visit('/managementportal');
    });

    it('should fail to login with bad password', () => {
        cy.get('h1').first()
            .should('have.text', 'Welcome to RADAR Management Portal');

        navBarPage.clickOnAccountMenu();
        navBarPage.clickOnSignIn();

        cy.get('input[name=identifier]').type('admin-email-here@radar-base.net')
        cy.get('input[name=password]').type('secret123')
        cy.get('button[type=submit]').click();

        let msg = 'Failed to sign in! Please check your credentials and try again.';
        cy.get('.alert-danger').first().should('have.text', msg);
        cy.get('#username').clear();
        cy.get('#password').clear();
    });

    it('should login successfully with admin account', () => {
        login('admin', 'admin')
    });

    it('should be able to update settings', () => {
        login('admin', 'admin')
        navBarPage.clickOnAccountMenu();
        navBarPage.clickOnEntity('settings');

        cy.get('h2').first()
            .should('have.text', 'User settings for [admin]');

        cy.get('button[type=submit]').click();

        cy.get('.alert-success').first()
            .should('have.text', 'Settings saved!');
    });
});
