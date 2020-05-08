import { browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('administration', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));

    beforeAll(async() => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        await browser.waitForAngular();
    });

    beforeEach(async() => {
        browser.sleep(1000);
        await navBarPage.clickOnAdminMenu();
    });

    it('should load user management', async() => {
        await navBarPage.clickOnEntity('user-management');
        const expect1 = /userManagement.home.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load metrics', async() => {
        await navBarPage.clickOnEntity('jhi-metrics');
        const expect1 = /metrics.title/;
        const pageTitle = element.all(by.css('h2 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load health', async() => {
        await navBarPage.clickOnEntity('jhi-health');
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

    it('should load audits', async() => {
        await navBarPage.clickOnEntity('audits');
        const expect1 = /audits.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

    it('should load logs', async() => {
        await navBarPage.clickOnEntity('logs');
        const expect1 = /logs.title/;
        const pageTitle = element.all(by.css('h2')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);
    });

});
