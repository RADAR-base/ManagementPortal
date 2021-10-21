import { login } from '../util/login';

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
        cy.contains('jhi-home .card-title', 'radar').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 3);
    });

    it('should load create Subject dialog', () => {
        cy.get('jhi-subjects button.create-subject').click();
        cy.get('jhi-subject-dialog h4.modal-title').first().should('have.text', 'Create or edit a Subject');
        cy.get('jhi-subject-dialog button.close').click();
    });

    it('should be able to create new subject', () => {
        cy.get('jhi-subjects button.create-subject').click();
        // cy.wait(1000);
        cy.get('jhi-subject-dialog input[name=externalLink]').type('https://radar-base-test.org');
        cy.get('jhi-subject-dialog input[name=externalId]').type('test-subject-1');
        cy.get('jhi-subject-dialog input[name=personName]').type('Test Subject 1');
        cy.get('jhi-subject-dialog input[name=dateOfBirth]').type('1980-01-01');
        cy.get('jhi-subject-dialog jhi-dictionary-mapper select').first().select('Human-readable-identifier');
        cy.get('jhi-subject-dialog jhi-dictionary-mapper input').first().type('Test Subject 1');
        cy.contains('jhi-subject-dialog jhi-dictionary-mapper button', 'Add').click()
        cy.contains('jhi-subject-dialog button.btn-primary', 'Save').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
    });

    it('should be able to edit a subject', () => {
        // cy.wait(1000);
        cy.contains('jhi-subjects .subject-row', 'test-subject-1').contains('button', 'Edit').click();
        // cy.wait(1000);
        cy.get('jhi-subject-dialog input[name=externalLink]').clear().type('https://radar-base-test-edited.org');
        cy.contains('jhi-subject-dialog button.btn-primary', 'Save').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
    });

    it('should have load subject row with subject-id, external-id, status, project, sources and attributes columns', () => {
        cy.get('jhi-subjects .subject-row').last().find('.subject-row__select-row input')
                .invoke('attr', 'type')
                .should('eq', 'checkbox')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__subject-id .subject-row__field-label')
                .should('have.text', 'Subject Id')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__subject-id a')
                .should('have.text','sub-3')
                .invoke('attr', 'href')
                .should('eq', '#/subject/sub-3')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__external-id .subject-row__field-label')
                .should('have.text', 'External Id')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__status .subject-row__field-label')
                .should('have.text', 'Status')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__status span.badge')
                .should('have.text','ACTIVATED')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__sources .subject-row__field-label')
                .should('have.text', 'Sources')

        // cy.get('jhi-subjects .subject-row').first().find('.subject-row__content .subject-row__sources a.form-control-static')
        //         .should('have.text','E4: source-1')
        //         .invoke('attr', 'href')
        //         .should('eq', '#/source/source-1')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__attribute-data .subject-row__field-label')
                .should('have.text', 'Attributes')

        cy.get('jhi-subjects .subject-row').last().find('.subject-row__content .subject-row__attribute-data div span')
                .should('have.text',' N/A ')
    })

    it('should be able to filter subjects by subject id', () => {
        cy.get('#field-subject-id').type('b-1');
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 1);
        cy.get('#field-subject-id').clear();
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
    });

    it('should be able to filter subjects by subject external id', () => {
        cy.get('#field-subject-external-id').type('test-subject-1');
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 1);
        cy.get('#field-subject-external-id').clear();
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
    });

    it('should be able to filter subjects by human readable id', () => {
        cy.get('#advanced-filter').click();
        cy.get('#field-human-readable-id').type('Test');
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 1);
        cy.get('#field-human-readable-id').clear();
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
        cy.get('#advanced-filter').click();
    });

    it('should be able to filter subjects by person name', () => {
        cy.get('#advanced-filter').click();
        cy.get('#field-person-name').type('Test');
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 1);
        cy.get('#field-person-name').clear();
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
        cy.get('#advanced-filter').click();
    });

    it('should be able to filter subjects by date of birth', () => {
        cy.get('#advanced-filter').click();
        cy.get('#field_date_of_birth').type('1980-01-01');
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 1);
        cy.get('#field_date_of_birth').clear();
        cy.get('#apply-filter').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 4);
        cy.get('#advanced-filter').click();
    });

    // TODO Filter by Enrollment Date

    it('should be able to delete a subject without source', () => {
        cy.contains('jhi-subjects .subject-row', 'test-subject-1').find('a').first().click();
        cy.contains('jhi-subject-detail button', 'Delete').click();
        cy.contains('jhi-subject-delete-dialog button', 'Delete').click();
        cy.get('jhi-subjects .subject-row').should('have.length', 3);
    });

    it('should be able to sort subjects by subject id in asc/desc order', () => {
        cy.get('jhi-subjects #field-order-by').click();
        cy.get('jhi-subjects #order-by-desc').click();
        cy.get('jhi-subjects .subject-row').first().should('contain.text', 'sub-3');
        cy.get('jhi-subjects #field-order-by').click();
        cy.get('jhi-subjects #order-by-asc').click();
        cy.get('jhi-subjects .subject-row').first().should('contain.text', 'sub-1');
    });
});
