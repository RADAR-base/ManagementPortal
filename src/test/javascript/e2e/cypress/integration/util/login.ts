import * as navBarPage from "./nav-bar";

export function login(username = "admin", password = "admin") {
    cy.visit('./');
    navBarPage.clickOnAccountMenu();
    navBarPage.clickOnSignIn();
    cy.get('.modal-content h1').first().should('have.text', 'Sign in');
    cy.get('#username').type(username);
    cy.get('#password').type(password);
    cy.get('button[type=submit]').click();
    cy.get('.alert-success span').should('exist');
    cy.getCookie('SESSION').should('exist')
}
