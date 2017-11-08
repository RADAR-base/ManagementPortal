import { browser, element, by, $ } from 'protractor';

describe('Create, assign, unassign and delete source', () => {

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
        element.all(by.partialLinkText('radar')).first().click().then(() => {
            expect(element(by.className('status-header')).getText()).toMatch('RADAR');
            // expect 3 subjects in this table
            element.all(by.css('subjects tbody tr')).count().then(function(count) {
                expect(count).toEqual(3);
            });
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
                element(by.cssContainingText('button.btn-primary', 'Save')).click().then(() => {
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
            element.all(by.cssContainingText('subjects tbody tr button', 'Pair Sources')).first().click().then(() => {
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

    it('should not be able to delete an assigned source', function () {
        element.all(by.linkText(sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Delete'))
            .click().then(() => {
                browser.waitForAngular();
                element(by.cssContainingText('.modal-footer button', 'Delete')).click().then(() => {
                    // if the delete succeeded the dialog will be disappeared and no Cancel button will be here anymore
                    element(by.buttonText('Cancel')).click();
                });
            });
    });

    it('should be able to unassign a source', function () {
        element(by.cssContainingText('li', 'Subjects')).click().then(() => {
            element.all(by.cssContainingText('subjects td a', sourceName))
            .all(by.xpath('ancestor::tr'))
            .all(by.cssContainingText('button', 'Pair Sources')).first().click().then(() => {
                browser.waitForAngular();
                element(by.cssContainingText('button', 'Remove')).click().then(() => {
                    browser.waitForAngular();
                    // source should be moved back to available sources table
                    element.all(by.css('source-assigner tbody')).get(0).all(by.css('tr')).then(function(rows) {
                        expect(rows.length).toEqual(0);
                    });
                    element.all(by.css('source-assigner tbody')).get(1).all(by.css('tr')).then(function(rows) {
                        expect(rows.length).toEqual(1);
                    });
                    element(by.cssContainingText('button', 'Save')).click().then(() => {
                        browser.waitForAngular();
                        // check that we have no cells in the subjects table containing the sourceName
                        element.all(by.cssContainingText('subjects td', sourceName)).count().then(function(count) {
                            expect(count).toBe(0);
                        });
                    });
                });
            });
        });
    });

    it('should be able to delete a source', function () {
        element(by.cssContainingText('li', 'Sources')).click().then(() => {
            element.all(by.linkText(sourceName))
                .all(by.xpath('ancestor::tr'))
                .all(by.cssContainingText('button', 'Delete'))
                .click().then(() => {
                    browser.waitForAngular();
                    element(by.cssContainingText('.modal-footer button', 'Delete')).click().then(() => {
                        browser.waitForAngular();
                        element.all(by.css('sources tbody tr')).count().then(function (count) {
                            expect(count).toBe(0);
                        });
                    });
                });
        });
    })

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
