export let clickOnAccountMenu = () => click('#account-menu');
export let clickOnAdminMenu = () => click('#admin-menu');
export let clickOnEntity = (name) => click('[routerLink="' + name + '"]');
export let clickOnEntityMenu = () => click('#entity-menu');
export let clickOnProjectMenu = () => click('#projects-menu');
export let clickOnSignIn = () => click('#login');

export let loadProjectView = () => {
    clickOnProjectMenu();
    cy.get('a').contains('radar').first().click();
    cy.get('.status-header').invoke('text').should('match', /RADAR/i);
};

function click(selector) {
    return cy.get(selector).click();
}
