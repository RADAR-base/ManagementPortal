
export let clickOnAccountMenu = () => click('#account-menu');
export let clickOnAdminMenu = () => click('#admin-menu');
export let clickOnEntity = (name) => click('[routerLink="' + name + '"]');
export let clickOnEntityMenu = () => click('#entity-menu');
export let clickOnProjectMenu = () => click('#projects-menu');
export let clickOnPasswordMenu = () => click('[routerLink="password"]');
export let clickOnSignIn = () => click('#login');
export let clickOnSignOut = () => click('#logout');

function click(selector) {
    return cy.get(selector).click();
}
