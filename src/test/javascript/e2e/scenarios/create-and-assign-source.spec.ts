import { $, browser, by, element } from 'protractor';

describe('Project View: Create, assign, unassign and delete source', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const projectMenu = element(by.id('projects-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));
    const sourceName = 'test-source-1';

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

        const pageTitle = element(by.className('status-header'));
        expect((await pageTitle.getText())).toMatch('RADAR');

        // expect 3 subjects in this table
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(3);
    });

    it('should be able to create a source', async () => {
        await element(by.cssContainingText('li', 'Sources')).click();
        await element(by.css('button.create-source')).click();
        await element(by.id('field_sourceName')).sendKeys(sourceName);
        await element(by.id('field_expectedSourceName')).sendKeys('AABBCC');
        await element.all(by.css('select option')).get(1).click();

        await element(by.cssContainingText('button.btn-primary', 'Save')).click();
        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-sources tbody tr')).count())).toEqual(2);
    });

    it('should be able to assign a source', async () => {
        await element(by.cssContainingText('li', 'Subjects')).click();
        await element.all(by.cssContainingText('jhi-subjects tbody tr td', 'sub-2'))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-subjects tbody tr button', 'Pair Sources'))
            .first().click();
        // first table lists assigned sources, this should be empty
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(0).all(by.css('tr')).count())).toEqual(0);
        // second table lists available sources, should have one element
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(1).all(by.css('tr')).count())).toEqual(1);

        await element(by.cssContainingText('button', 'Add')).click();
        await browser.waitForAngular();
        // available source should be moved to first table
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(0).all(by.css('tr')).count())).toEqual(1);
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(1).all(by.css('tr')).count())).toEqual(0);

        await element(by.cssContainingText('button', 'Save')).click();
        await browser.waitForAngular();
        // check that we have exactly one cell in the subjects table containing the sourceName
        expect((await element.all(by.cssContainingText('jhi-subjects td', sourceName)).count())).toBe(1);
    });

    it('should show the source as assigned', async () => {
        await element(by.cssContainingText('li', 'Sources')).click();
        expect((await element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('.badge-success', 'Assigned'))
            .count())).toBe(1);
    });

    it('should not be able to delete an assigned source', async () => {
        await element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Delete')).first()
            .click();
        await browser.waitForAngular();
        await element(by.cssContainingText('.modal-footer button', 'Delete')).click();
        // if the delete succeeded the dialog will be disappeared and no Cancel button will be here anymore
        await element(by.buttonText('Cancel')).click();
    });

    it('should be able to unassign a source', async () => {
        await element(by.cssContainingText('li', 'Subjects')).click();
        await element.all(by.cssContainingText('jhi-subjects td a', sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Pair Sources')).first().click();
        await browser.waitForAngular();
        await element(by.cssContainingText('button', 'Remove')).click();
        await browser.waitForAngular();
        // source should be moved back to available sources table

        expect((await element.all(by.css('jhi-source-assigner tbody')).get(0).all(by.css('tr')).count())).toEqual(0);
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(1).all(by.css('tr')).count())).toEqual(1);

        await element(by.cssContainingText('button', 'Save')).click();
        await browser.waitForAngular();
        // check that we have no cells in the subjects table containing the sourceName
        expect((await element.all(by.cssContainingText('jhi-subjects td', sourceName)).count())).toBe(0);
    });

    it('should not be able to delete a source used by subject', async () => {
        await element(by.cssContainingText('li', 'Sources')).click();
        await element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Delete'))
            .click();

        await browser.waitForAngular();
        await element(by.cssContainingText('.modal-footer button', 'Delete')).click();

        await browser.waitForAngular();
        await element(by.buttonText('Cancel')).click();
    });

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
