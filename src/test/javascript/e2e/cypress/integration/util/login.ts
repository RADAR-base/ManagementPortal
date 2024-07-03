export function login() {
    cy.login('admin-email-here@radar-base.net', 'secret123');
    cy.wait(2000);
    cy.visit('managementportal/');
}
