import { login } from '../util/login';
import * as navBarPage from '../util/nav-bar';

describe('Project view: Generate QR code', () => {
    before(() => {
        login();
        cy.visit('./');
    });

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('oAtkn');
    });

    it('should load project view', async() => {
        await navBarPage.clickOnProjectMenu();
        await element.all(by.partialLinkText('radar')).first().click();
        expect(element(by.className('status-header')).getText()).toMatch('RADAR');
        // expect 3 subjects in this table
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(3);
    });

    it('should open pair app dialog', async() => {
        await element.all(by.buttonText('Pair App')).first().click();
        expect(element.all(by.name('pairForm')).all(by.css('h4')).first().getAttribute('jhitranslate'))
            .toMatch('managementPortalApp.subject.home.pairAppLabel');
    });

    it('should be able to create a qr code', async() => {
        await element(by.name('pairForm')).element(by.cssContainingText('option', 'pRMT')).click();
        await element(by.buttonText('Generate QR code')).click();
        expect((await element.all(by.css('qrcode')).count())).toBe(1);
        await element(by.css('button.close')).click();
    });

});
