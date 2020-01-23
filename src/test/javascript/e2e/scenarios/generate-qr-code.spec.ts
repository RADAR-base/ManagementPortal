import { browser, by, element } from 'protractor';

describe('Project view: Generate QR code', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const projectMenu = element(by.id('projects-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    beforeAll(async () => {
        await browser.get('#');

        await accountMenu.click();
        await login.click();

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
        await browser.waitForAngular();
    });

    beforeEach(async () => {
        browser.sleep(1000);
    });

    it('should load project view', async () => {
        await projectMenu.click();
        await element.all(by.partialLinkText('radar')).first().click();
        expect(element(by.className('status-header')).getText()).toMatch('RADAR');
        // expect 3 subjects in this table
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(3);
    });

    it('should open pair app dialog', async () => {
        await element.all(by.buttonText('Pair App')).first().click();
        expect(element.all(by.name('pairForm')).all(by.css('h4')).first().getAttribute('jhitranslate'))
            .toMatch('managementPortalApp.subject.home.pairAppLabel');
    });

    it('should be able to create a qr code', async () => {
        await element(by.name('pairForm')).element(by.cssContainingText('option', 'pRMT')).click();
        expect((await element.all(by.css('qrcode')).count())).toBe(1);
        await element(by.css('button.close')).click();
    });

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
