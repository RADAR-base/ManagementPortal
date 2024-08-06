export function login() {
    cy.login(Cypress.env('username'), Cypress.env('secret'));
    cy.wait(2000);
    cy.visit('managementportal/');
}
