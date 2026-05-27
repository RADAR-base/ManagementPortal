import * as navBarPage from "./nav-bar";

export function login(username = "admin", password = "admin") {
    cy.visit('/');

    // Click on account menu and sign in
    navBarPage.clickOnAccountMenu();
    navBarPage.clickOnSignIn();

    // Wait for redirect to login page (could be internal or external OAuth)
    cy.wait(100);

    // The app will redirect to /api/redirect/login which then redirects to OAuth
    // For internal auth, we should see a login form or be redirected back
    // Let's wait for the page to settle
    cy.url({ timeout: 10000 }).should('include', 'localhost');

    // If there's a login form visible, fill it
    cy.get('body').then(($body) => {
        if ($body.find('#username').length > 0) {
            // Login form is present (internal auth)
            cy.get('#username').type(username);
            cy.get('#password').type(password);
            cy.get('button[type=submit]').click();
            cy.wait(2000);
        }
    });

    // Verify session is established
    cy.getCookie('SESSION').should('exist');
}
