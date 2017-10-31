import { browser, element, by, $ } from 'protractor';

describe('Generate QR code', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const projectMenu = element(by.id('projects-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    beforeAll(() => {
        browser.get('/');

        accountMenu.click();
        login.click();

        username.sendKeys('admin');
        password.sendKeys('admin');
        element(by.css('button[type=submit]')).click();
        browser.waitForAngular();
    });

    it('should load project view', function () {
        projectMenu.click();
        element.all(by.partialLinkText('radar')).first().click().then(() => {
            expect(element(by.className('status-header')).getText()).toMatch('RADAR');
            // expect 3 subjects in this table
            element.all(by.css('subjects tbody tr')).count().then(function(count) {
                expect(count).toEqual(3);
            });
        });
    });

    it('should open pair app dialog', function () {
        element.all(by.buttonText('Pair App')).first().click().then(() => {
            expect(element.all(by.name('pairForm')).all(by.css('h4')).first().getAttribute('jhitranslate'))
                .toMatch('managementPortalApp.subject.home.pairAppLabel');
        });
    });

    it('should be able to create a qr code', function () {
        element(by.name('pairForm')).element(by.cssContainingText('option', 'pRMT')).click().then(() => {
            element.all(by.css('qr-code')).count().then(function(count) {
                expect(count).toBe(1);
            });
            element(by.css('button.close')).click();
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
