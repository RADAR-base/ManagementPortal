import { $, browser, by, element } from 'protractor';

import { NavBarPage } from '../page-objects/jhi-page-objects';

describe('Project view: Create a subject, assign sources, discontinue and delete a subject.', () => {
    let navBarPage: NavBarPage;
    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const sourceName = 'test-source-2';
    const externalId = 'test-id';

    beforeAll(async() => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        await browser.waitForAngular();
    });

    beforeEach(async() => {
        browser.sleep(1000);
    });

    it('should load project view', async() => {
        await navBarPage.clickOnProjectMenu();
        await element.all(by.partialLinkText('radar')).first().click();
    });

    it('should be able to create a subject', async() => {
        await element(by.cssContainingText('jhi-subjects h4 button.btn-primary', 'Create a new Subject')).click();
        await element(by.name('externalId')).sendKeys(externalId);
        await element(by.cssContainingText('button.btn-primary', 'Save')).click();

        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(4);
    });

    it('should be able to create a source', async() => {
        await element(by.cssContainingText('li', 'Sources')).click();
        await element(by.css('button.create-source')).click();
        await element(by.id('field_sourceName')).sendKeys(sourceName);
        await element(by.id('field_expectedSourceName')).sendKeys('AABBCC');
        await element.all(by.css('select option')).get(1).click();

        await element(by.css('.modal-footer button.btn-primary')).click();

        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-sources tbody tr')).count())).toEqual(3);
    });

    it('should be able to assign a source', async() => {
        await element(by.cssContainingText('li', 'Subjects')).click();
        // find a row which contains the external-id entered
        await element.all(by.cssContainingText('jhi-subjects tbody tr td', externalId))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-subjects tbody tr button', 'Pair Sources')).click();

        // first table lists assigned sources, this should be empty
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(0).all(by.css('tr')).count())).toEqual(0);
        // second table lists available sources, should have two elements
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(1).all(by.css('tr')).count())).toEqual(2);

        // element(by.xpath("//table/tr[td = 'Eve']")).click();
        const source = element.all(by.xpath('//tr/td[2]')).filter(el => el.getText().then((text) => text.match(sourceName) !== null)).first()
            .element(by.xpath('ancestor::tr'));
        await source.element(by.cssContainingText('button', 'Add')).click();
        await browser.waitForAngular();
        // available source should be moved to first table
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(0).all(by.css('tr')).count())).toEqual(1);
        expect((await element.all(by.css('jhi-source-assigner tbody')).get(1).all(by.css('tr')).count())).toEqual(1);

        await element(by.cssContainingText('button', 'Save')).click();
        browser.waitForAngular();
        // check that we have exactly one cell in the subjects table containing the sourceName
        expect((await element.all(by.cssContainingText('jhi-subjects td', sourceName)).count())).toBe(1);

    });

    it('should show the source as assigned', async() => {
        await element(by.cssContainingText('li', 'Sources')).click();
        expect(await element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('.badge-success', 'Assigned'))
            .count()).toBe(1);
    });

    it('should be able to discontinue a subject', async() => {
        await element(by.cssContainingText('li', 'Subjects')).click();
        // find a row which contains a uuid
        await element.all(by.cssContainingText('jhi-subjects tbody tr td', externalId))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-subjects tbody tr button', 'Discontinue')).click();
        await browser.waitForAngular();
        expect(element(by.css('h4.modal-title')).getAttribute('jhitranslate')).toMatch('managementPortalApp.subject.discontinue.title');

        await element(by.name('deleteForm')).element(by.buttonText('Discontinue')).click();
        await browser.waitForAngular();
    });

    it('should show the source as unassigned', async() => {
        await element(by.cssContainingText('li', 'Sources')).click();
        expect(await element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('.badge-danger', 'Unassigned'))
            .count()).toBe(1);
    });

    it('should be able to delete a subject', async() => {
        await element(by.cssContainingText('li', 'Subjects')).click();
        await element.all(by.cssContainingText('jhi-subjects tbody tr td', externalId))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('jhi-subjects tbody tr button', 'Delete'))
            .click();

        await browser.waitForAngular();

        expect(element(by.css('h4.modal-title')).getAttribute('jhitranslate')).toMatch('entity.delete.title');
        await element(by.css('.modal-footer button.btn-danger')).click();

        await browser.waitForAngular();
        expect((await element.all(by.css('jhi-subjects tbody tr')).count())).toEqual(3);
    });
});
