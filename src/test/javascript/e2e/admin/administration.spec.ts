import { browser, by, element } from 'protractor';

describe('administration', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const accountMenu = element(by.id('account-menu'));
    const adminMenu = element(by.id('admin-menu'));
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
        await adminMenu.click();
        browser.sleep(1000);
    });

    it('should load user management', async () => {
        await element(by.css('[routerLink="user-management"]')).click();
        const expect1 = /userManagement.home.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load metrics', async () => {
        await element(by.css('[routerLink="jhi-metrics"]')).click();
        const expect1 = /metrics.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load health', async () => {
        await element(by.css('[routerLink="jhi-health"]')).click();
        const expect1 = /health.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    // it('should load configuration', () => {
    //     element(by.css('[routerLink="jhi-configuration"]')).click();
    //     const expect1 = /configuration.title/;
    //     element.all(by.css('h2')).first().getAttribute('jhiTranslate').then((value) => {
    //         expect(value).toMatch(expect1);
    //     });
    // });

    it('should load audits', async () => {
        await element(by.css('[routerLink="audits"]')).click();
        const expect1 = /audits.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load logs', async () => {
        await element(by.css('[routerLink="logs"]')).click();
        const expect1 = /logs.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
