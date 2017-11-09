import { browser, element, by, $ } from 'protractor';

describe('Discontinued subject should unassign sources', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const projectMenu = element(by.id('projects-menu'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));
    const sourceName = 'test-source-1';

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
        element.all(by.partialLinkText('radar')).first().click();
    });

    it('should be able to create a subject', function () {
        element(by.css('subjects h4 button.btn-primary')).click().then(() => {
            element(by.css('.modal-footer button.btn-primary')).click();
        });
    });

    it('should be able to create a source', function () {
        element(by.cssContainingText('li', 'Sources')).click().then(() => {
            element(by.css('button.create-source')).click().then(() => {
                element(by.id('field_sourceName')).sendKeys(sourceName);
                element(by.id('field_expectedSourceName')).sendKeys('AABBCC');
                element.all(by.css('select option')).then(function(options) {
                    options[1].click();
                });
                element(by.css('.modal-footer button.btn-primary')).click().then(() => {
                    browser.waitForAngular();
                    element.all(by.css('sources tbody tr')).count().then(function(count) {
                        expect(count).toEqual(1);
                    });
                });
            });
        });
    });

    it('should be able to assign a source', function () {
        element(by.cssContainingText('li', 'Subjects')).click().then(() => {
            // find a row which contains a uuid
            const row = element.all(by.xpath('//tr/td')).filter(el => el.getText().then((text) => text.match(/[a-z0-9\-]{36}/) != null)).first()
                .element(by.xpath('ancestor::tr'));
            row.element(by.buttonText('Pair Sources')).click().then(() => {
                // first table lists assigned sources, this should be empty
                element.all(by.css('source-assigner tbody')).get(0).all(by.css('tr')).then(function(rows) {
                    expect(rows.length).toEqual(0);
                });
                // second table lists available sources, should have one element
                element.all(by.css('source-assigner tbody')).get(1).all(by.css('tr')).then(function(rows) {
                    expect(rows.length).toEqual(1);
                });
                element(by.cssContainingText('button', 'Add')).click().then(() => {
                    browser.waitForAngular();
                    // available source should be moved to first table
                    element.all(by.css('source-assigner tbody')).get(0).all(by.css('tr')).then(function(rows) {
                        expect(rows.length).toEqual(1);
                    });
                    element.all(by.css('source-assigner tbody')).get(1).all(by.css('tr')).then(function(rows) {
                        expect(rows.length).toEqual(0);
                    });
                    element(by.cssContainingText('button', 'Save')).click().then(() => {
                        browser.waitForAngular();
                        // check that we have exactly one cell in the subjects table containing the sourceName
                        element.all(by.cssContainingText('subjects td', sourceName)).count().then(function(count) {
                            expect(count).toBe(1);
                        });
                    });
                });
            });
        });
    });

    it('should show the source as assigned', function () {
        element(by.cssContainingText('li', 'Sources')).click().then(() => {
            element.all(by.linkText(sourceName))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('.badge-success', 'Assigned'))
                .count().then(function (count) {
                    expect(count).toBe(1);
                });
        });
    });

    it('should be able to discontinue a subject', function () {
        element(by.cssContainingText('li', 'Subjects')).click().then(() => {
            // find a row which contains a uuid
            const row = element.all(by.xpath('//tr/td')).filter(el => el.getText().then((text) => text.match(/[a-z0-9\-]{36}/) != null)).first()
                .element(by.xpath('ancestor::tr'));
            row.element(by.buttonText('Discontinue')).click().then(() => {
                expect(element(by.css('h4.modal-title')).getAttribute('jhitranslate')).toMatch('managementPortalApp.subject.discontinue.title');
                element(by.name('deleteForm')).element(by.buttonText('Discontinue')).click();
            });
        });
    });

    it('should show the source as unassigned', function () {
        element(by.cssContainingText('li', 'Sources')).click().then(() => {
            element.all(by.linkText(sourceName))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('.badge-danger', 'Unassigned'))
                .count().then(function (count) {
                    expect(count).toBe(1);
                });
        });
    });

    it('should be able to delete a source', function () {
        element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Delete'))
            .click().then(() => {
                element(by.css('.modal-footer button.btn-danger')).click().then(() => {
                    browser.waitForAngular();
                    element.all(by.css('sources tbody tr')).count().then(function (count) {
                        expect(count).toBe(0);
                    });
                });
            });
    });

    it('should be able to delete a subject', function () {
        element(by.cssContainingText('li', 'Subjects')).click().then(() => {
            // find a row which contains a uuid
            const row = element.all(by.xpath('//tr/td')).filter(el => el.getText().then((text) => text.match(/[a-z0-9\-]{36}/) != null)).first()
                .element(by.xpath('ancestor::tr'));
            row.element(by.buttonText('Delete')).click().then(() => {
                expect(element(by.css('h4.modal-title')).getAttribute('jhitranslate')).toMatch('entity.delete.title');
                element(by.css('.modal-footer button.btn-danger')).click();
            });
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
