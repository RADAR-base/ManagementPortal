import { $, browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('Subject e2e test', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));

    beforeAll(async () => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);

        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignIn();

        await username.sendKeys('admin');
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
        await browser.waitForAngular();
    });

    beforeEach(async () => {
        browser.sleep(1000);
    });

    it('should load Subjects', async () => {
        await navBarPage.clickOnEntityMenu();
        await element.all(by.css('[routerLink="subject"]')).first().click();

        const expectVal = /managementPortalApp.subject.home.title/;
        const pageTitle = element.all(by.css('h4 span')).first();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);
    });

    it('should load create Subject dialog', async () => {
        await element(by.css('button.create-subject')).click();
        const expectVal = /managementPortalApp.subject.home.createOrEditLabel/;

        const modalTitle = element.all(by.css('h4.modal-title')).first();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expectVal);

        await element(by.css('button.close')).click();
    });

    it('should be able to create new subject', async () => {
        await element(by.cssContainingText('button.btn-primary', 'Create a new Subject')).click();

        await element(by.name('externalId')).sendKeys('test-subject1');
        await element(by.name('project')).sendKeys('radar');

        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(5);
    });

    it('should be able to edit a subject', async () => {
        await element.all(by.cssContainingText('jhi-subjects tbody tr td', 'test-subject1'))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-subjects tbody tr button', 'Edit'))
            .first().click();
        await element(by.name('externalLink')).sendKeys('www.radarcns.org');
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
        await browser.waitForAngular();

        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(5);
    });

    it('should be able to delete a subject without source', async () => {
        await element(by.cssContainingText('jhi-subjects tbody tr td', 'test-subject1'))
            .element(by.xpath('ancestor::tr'))
            .element(by.cssContainingText('button', 'Delete')).click();

        await browser.waitForAngular();
        await element(by.cssContainingText('jhi-subject-delete-dialog button.btn-danger', 'Delete'))
            .click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(4);
    });

    afterAll(async () => {
        await browser.waitForAngular();
        await navBarPage.clickOnAccountMenu();
        await navBarPage.clickOnSignOut();
        browser.sleep(1000);
    });
});
