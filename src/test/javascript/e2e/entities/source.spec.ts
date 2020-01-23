import { $, browser, by, element } from 'protractor';

describe('Source e2e test', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const entityMenu = element(by.id('entity-menu'));
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

    it('should load Sources', async () => {
        await entityMenu.click();
        await element.all(by.css('[routerLink="source"]')).first().click();

        const expectVal = /managementPortalApp.source.home.title/;
        const pageTitle = element.all(by.css('h4 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);
    });

    it('should load create Source dialog', async () => {
        await element(by.css('button.create-source')).click();

        const expectVal = /managementPortalApp.source.home.createOrEditLabel/;
        const modalTitle = element.all(by.css('h4.modal-title')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);

        await element(by.css('button.close')).click();
    });

    it('should be able to create new source', async () => {
        await element(by.cssContainingText('button.btn-primary', 'Create a new Source')).click();
        await element(by.name('sourceName')).sendKeys('test-source1');
        await element(by.name('expectedSourceName')).sendKeys('A007C');
        await element(by.name('project')).sendKeys('radar');
        await element(by.name('sourceType')).sendKeys('Empatica_E4_v1');

        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-source tbody tr')).count())).toEqual(2);
    });

    it('should be able to edit a source', async () => {
        await element.all(by.cssContainingText('jhi-source tbody tr td', 'test-source1'))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-source tbody tr button', 'Edit'))
            .first().click();
        await element(by.name('expectedSourceName')).sendKeys('A007C9');
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-source tbody tr')).count())).toEqual(2);
    });

    it('should be able to delete an unassigned source', async () => {
        await element(by.cssContainingText('jhi-source tbody tr td', 'test-source1'))
            .element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Delete')).click();
        await browser.waitForAngular();
        await element(by.cssContainingText('jhi-source-delete-dialog button.btn-danger', 'Delete'))
            .click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-source tbody tr')).count())).toEqual(1);
    });

    // Source creation and deletion already covered in scenarios/create-and-assign-source.spec.ts

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
