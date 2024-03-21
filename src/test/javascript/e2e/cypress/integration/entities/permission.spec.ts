import { login } from '../util/login';

describe('Organization Permissions e2e test', () => {
    beforeEach(() => {
        login();
        cy.contains('jhi-home .card-title', 'main').click();
        cy.contains('jhi-organization-detail ul.nav-tabs .nav-item', 'Permissions').click();

        // cy.contains('jhi-projects table tbody td a', 'radar').click();
    });

    it('should load Permissions', () => {
        cy.get('jhi-permissions tbody tr').should('have.length', 3);
    });

    it('should add user with role organization admin', () => {
        cy.get('jhi-permissions select#user').select('padmin');
        cy.get('jhi-permissions button.fa-plus').click();
        cy.get('jhi-permissions tbody tr').should('have.length', 3);
    })

    it('should remove user with role organization admin', () => {
        cy.wait(100)
        cy.get('jhi-permissions button.fa-remove').eq(1).click({force: true});
        cy.get('jhi-permissions tbody tr').should('have.length', 2);
    })


});

// describe('Project Permissions e2e test', () => {
//     beforeEach(() => {
//         login();
//         cy.contains('jhi-home .card-title', 'main').click();
//         cy.contains('jhi-organization-detail ul.nav-tabs .nav-item', 'Permissions').click();
//
//         // cy.contains('jhi-projects table tbody td a', 'radar').click();
//     });
//
//     it('should load Permissions', () => {
//         cy.get('jhi-permissions tbody tr').should('have.length', 2);
//     });
//
//     it('should add user with role organization admin', () => {
//         cy.get('jhi-permissions select#user').select('padmin');
//         cy.get('jhi-permissions button.fa-plus').click();
//         cy.get('jhi-permissions tbody tr').should('have.length', 3);
//     })
//
//     it('should remove user with role organization admin', () => {
//         cy.get('jhi-permissions button.fa-remove').eq(1).click();
//         cy.get('jhi-permissions tbody tr').should('have.length', 2);
//     })
//
//
// });
