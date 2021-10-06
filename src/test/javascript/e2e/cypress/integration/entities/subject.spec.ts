import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Subject e2e test', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        cy.wait(100);
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load Subjects', () => {
        navBarPage.clickOnEntityMenu();
        navBarPage.clickOnEntity('subject');

        cy.get('h2 span').first().should('have.text', 'Subjects');
    });

    it('should load create Subject dialog', () => {
        cy.get('button.create-subject').click();

        cy.get('h4.modal-title').first()
            .should('have.text', 'Create or edit a Subject');

        cy.get('button.close').click();
    });

    it('should be able to create new subject', () => {
        cy.get('button.btn-primary').contains('Create a new Subject').click();
        cy.wait(1000);

        cy.get('[name=externalId]').type('test-subject1');
        cy.get('[name=project]').select('radar');

        cy.get('button.btn-primary').contains('Save').click();
        cy.get('jhi-subjects .subjects .subject-row').should('have.length', 5);
    });

    it('should be able to edit a subject', () => {
        cy.wait(1000);
        cy.get('jhi-subjects .subjects .subject-row .subject-row__external-id div span').contains('test-subject1')
                .parents('.subject-row')
                .get('.subject-row__actions button span.hidden-md-down').contains('Edit')
                .first().click();
        cy.get('[name=externalLink]').type('www.radar-base.org');
        cy.get('button.btn-primary').contains('Save').click();

        cy.get('jhi-subjects .subjects .subject-row').should('have.length', 5);
    });

    it('should be able to delete a subject without source', () => {
        cy.get('jhi-subjects .subjects .subject-row .subject-row__external-id div span').contains('test-subject1')
                .parents('.subject-row').find('button').contains('View').click();

        cy.get('jhi-subject-detail button span.hidden-md-down').contains('Delete').click();
        cy.get('jhi-subject-delete-dialog button.btn-danger')
                .contains('Delete').click();
        cy.get('jhi-subjects .subjects .subject-row').should('have.length', 4);
    });

    it('should have load subject row with subject-id, external-id, status, project, sources and attributes columns', () => {
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__select-row input')
                .invoke('attr', 'type')
                .should('eq', 'checkbox')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__subject-id .subject-row__field-label')
                .should('have.text', 'Subject Id')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__subject-id a')
                .should('have.text','sub-1')
                .invoke('attr', 'href')
                .should('eq', '#/subject/sub-1')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__external-id .subject-row__field-label')
                .should('have.text', 'External Id')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__external-id a')
                .should('contain.text','www.radar-base.org')
                .invoke('attr', 'href')
                .should('eq', 'www.radar-base.org')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__status .subject-row__field-label')
                .should('have.text', 'Status')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__status span.badge')
                .should('have.text','ACTIVATED')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__project .subject-row__field-label')
                .should('have.text', 'Project')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__project a.badge')
                .should('have.text','radar')
                .invoke('attr', 'href')
                .should('eq', '#/project/radar')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__sources .subject-row__field-label')
                .should('have.text', 'Sources')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__sources a.form-control-static')
                .should('have.text','E4: source-1')
                .invoke('attr', 'href')
                .should('eq', '#/source/source-1')

        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__attribute-data .subject-row__field-label')
                .should('have.text', 'Attributes')
        cy.get('jhi-subjects .subjects .subject-row').first().find('.subject-row__content .subject-row__attribute-data div span')
                .should('have.text',' N/A ')
    })

});
