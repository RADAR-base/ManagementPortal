import { browser, by, element } from 'protractor';

describe('account', () => {

    const username = element(by.id('username'));
    const password = element(by.id('password'));
    const accountMenu = element(by.id('account-menu'));
    const login = element(by.id('login'));
    const logout = element(by.id('logout'));

    let originalTimeOut;

    beforeAll(async () => {
        await browser.get('#');
    });

    beforeEach(async () => {
        originalTimeOut = jasmine.DEFAULT_TIMEOUT_INTERVAL;
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 240000;
        browser.sleep(1000);
    });

    it('should fail to login with bad password', async () => {
        const expect1 = /home.title/;
        const header = element.all(by.css('h1')).first();
        await header.isPresent();
        expect((await header.getAttribute('jhiTranslate'))).toMatch(expect1);

        await accountMenu.click();
        await login.click();

        await username.sendKeys('admin');
        await password.sendKeys('foo');
        await element(by.css('button[type=submit]')).click();

        const expect2 = /login.messages.error.authentication/;
        const alertMessage = element.all(by.css('.alert-danger')).first();
        await alertMessage.isPresent();
        expect((await alertMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
    });

    it('should login successfully with admin account', async () => {
        const expect1 = /login.title/;
        const modalTitle = element.all(by.css('.modal-content h1')).first();
        await modalTitle.isPresent();
        expect((await modalTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await username.clear();
        await username.sendKeys('admin');
        await password.clear();
        await password.sendKeys('admin');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();

        const expect2 = /home.logged.message/;
        const successMessage = element.all(by.css('.alert-success span'));
        await successMessage.isPresent();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
    });

    it('should be able to update settings', async () => {
        await accountMenu.click();
        await element(by.css('[routerLink="settings"]')).isPresent();
        await element(by.css('[routerLink="settings"]')).click();

        const expect1 = /settings.title/;
        const pageTitle = element.all(by.css('h2')).first();
        await pageTitle.isPresent();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await element(by.css('button[type=submit]')).click();

        const expect2 = /settings.messages.success/;
        const successMessage = element.all(by.css('.alert-success')).first();
        await successMessage.isPresent();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);
    });

    it('should be able to update password', async () => {
        await accountMenu.click();
        await element(by.css('[routerLink="password"]')).isPresent();
        await element(by.css('[routerLink="password"]')).click();

        const expect1 = /password.title/;
        const pageTitle = element.all(by.css('h2')).first();
        await pageTitle.isPresent();
        expect((await pageTitle.getAttribute('jhiTranslate'))).toMatch(expect1);

        await password.sendKeys('newpassword');
        await element(by.id('confirmPassword')).sendKeys('newpassword');
        await element(by.css('button[type=submit]')).click();

        const expect2 = /password.messages.success/;
        const successMessage = element.all(by.css('.alert-success')).first();
        await successMessage.isPresent();
        expect((await successMessage.getAttribute('jhiTranslate'))).toMatch(expect2);

        await accountMenu.click();
        await logout.click();

        await accountMenu.click();
        await login.click();

        await username.sendKeys('admin');
        await password.sendKeys('newpassword');
        await element(by.css('button[type=submit]')).click();

        await browser.waitForAngular();

        await accountMenu.click();
        await await element(by.css('[routerLink="password"]')).isPresent();
        await element(by.css('[routerLink="password"]')).click();
        // change back to default
        await password.clear();
        await password.sendKeys('admin');
        await element(by.id('confirmPassword')).clear();
        await element(by.id('confirmPassword')).sendKeys('admin');
        await element(by.css('button[type=submit]')).click();
    });

    afterEach(() => {
        jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeOut;
    });

    afterAll(async () => {
        await accountMenu.click();
        await logout.click();
        browser.sleep(1000);
    });
});
