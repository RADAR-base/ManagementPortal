import { ElementFinder, by, element } from 'protractor';
export class NavBarPage {
    entityMenu = element(by.id('entity-menu'));
    accountMenu = element(by.id('account-menu'));
    projectMenu = element(by.id('projects-menu'));
    adminMenu: ElementFinder;
    signIn = element(by.id('login'));
    register = element(by.css('[routerLink="register"]'));
    signOut = element(by.id('logout'));
    passwordMenu = element(by.css('[routerLink="password"]'));
    settingsMenu = element(by.css('[routerLink="settings"]'));

    constructor(asAdmin?: Boolean) {
        if (asAdmin) {
            this.adminMenu = element(by.id('admin-menu'));
        }
    }

    async clickOnEntityMenu() {
        await this.entityMenu.isPresent();
        return this.entityMenu.click();
    }

    async clickOnAccountMenu() {
        await this.accountMenu.isPresent();
        return this.accountMenu.click();
    }

    async clickOnAdminMenu() {
        await this.adminMenu.isPresent();
        return this.adminMenu.click();
    }

    async clickOnProjectMenu() {
        await this.projectMenu.isPresent();
        return this.projectMenu.click();
    }

    async clickOnSignIn() {
        await this.signIn.isPresent();
        return this.signIn.click();
    }

    async clickOnRegister() {
        await this.signIn.isPresent();
        return this.signIn.click();
    }

    async clickOnSignOut() {
        await this.signOut.isPresent();
        return this.signOut.click();
    }

    async clickOnPasswordMenu() {
        await this.passwordMenu.isPresent();
        return this.passwordMenu.click();
    }

    async clickOnSettingsMenu() {
        await this.settingsMenu.isPresent();
        return this.settingsMenu.click();
    }

    async clickOnEntity(entityName: string) {
        await element(by.css('[routerLink="' + entityName + '"]')).isPresent();
        return element(by.css('[routerLink="' + entityName + '"]')).click();
    }

    async clickOnAdmin(entityName: string) {
        await element(by.css('[routerLink="' + entityName + '"]')).isPresent();
        return element(by.css('[routerLink="' + entityName + '"]')).click();
    }

    async getSignInPage() {
        await this.clickOnAccountMenu();
        await this.clickOnSignIn();
        return new SignInPage();
    }

    async getPasswordPage() {
        await this.clickOnAccountMenu();
        await this.clickOnPasswordMenu();
        return new PasswordPage();
    }

    async getSettingsPage() {
        await this.clickOnAccountMenu();
        await this.clickOnSettingsMenu();
        return new SettingsPage();
    }

    async goToEntity(entityName: string) {
        await this.clickOnEntityMenu();
        return this.clickOnEntity(entityName);
    }

    async goToSignInPage() {
        await this.clickOnAccountMenu();
        await this.clickOnSignIn();
    }

    async goToPasswordMenu() {
        await this.clickOnAccountMenu();
        await this.clickOnPasswordMenu();
    }

    async autoSignOut() {
        await this.clickOnAccountMenu();
        await this.clickOnSignOut();
    }
}

export class SignInPage {
    username = element(by.id('username'));
    password = element(by.id('password'));
    loginButton = element(by.css('button[type=submit]'));

    async setUserName(username) {
        await this.username.isPresent();
        this.username.sendKeys(username);
    }

    async getUserName() {
        await this.username.isPresent();
        return this.username.getAttribute('value');
    }

    async clearUserName() {
        await this.username.isPresent();
        this.username.clear();
    }

    async setPassword(password) {
        await this.password.isPresent();
        this.password.sendKeys(password);
    }

    async getPassword() {
        await this.password.isPresent();
        return this.password.getAttribute('value');
    }

    async clearPassword() {
        await this.password.isPresent();
        this.password.clear();
    }

    async autoSignInUsing(username: string, password: string) {
        await this.setUserName(username);
        await this.setPassword(password);
        return this.login();
    }

    async login() {
        await this.loginButton.isPresent();
        return this.loginButton.click();
    }
}

export class PasswordPage {
    password = element(by.id('password'));
    confirmPassword = element(by.id('confirmPassword'));
    saveButton = element(by.css('button[type=submit]'));
    title = element.all(by.css('h2')).first();

    async setPassword(password) {
        await this.password.isPresent();
        this.password.sendKeys(password);
    }

    async getPassword() {
        await this.password.isPresent();
        return this.password.getAttribute('value');
    }

    async clearPassword() {
        await this.password.isPresent();
        this.password.clear();
    }

    async setConfirmPassword(confirmPassword) {
        await this.confirmPassword.isPresent();
        this.confirmPassword.sendKeys(confirmPassword);
    }

    async getConfirmPassword() {
        await this.confirmPassword.isPresent();
        return this.confirmPassword.getAttribute('value');
    }

    async clearConfirmPassword() {
        await this.confirmPassword.isPresent();
        this.confirmPassword.clear();
    }

    async getTitle() {
        await this.title.isPresent();
        return this.title.getAttribute('jhiTranslate');
    }

    async save() {
        await this.saveButton.isPresent();
        return this.saveButton.click();
    }
}

export class SettingsPage {
    firstName = element(by.id('firstName'));
    lastName = element(by.id('lastName'));
    email = element(by.id('email'));
    saveButton = element(by.css('button[type=submit]'));
    title = element.all(by.css('h2')).first();

    async setFirstName(firstName) {
        await this.firstName.isPresent();
        this.firstName.sendKeys(firstName);
    }

    async getFirstName() {
        await this.firstName.isPresent();
        return this.firstName.getAttribute('value');
    }

    async clearFirstName() {
        await this.firstName.isPresent();
        this.firstName.clear();
    }

    async setLastName(lastName) {
        await this.lastName.isPresent();
        this.lastName.sendKeys(lastName);
    }

    async getLastName() {
        await this.lastName.isPresent();
        return this.lastName.getAttribute('value');
    }

    async clearLastName() {
        await this.lastName.isPresent();
        this.lastName.clear();
    }

    async setEmail(email) {
        await this.email.isPresent();
        this.email.sendKeys(email);
    }

    async getEmail() {
        await this.email.isPresent();
        return this.email.getAttribute('value');
    }

    async clearEmail() {
        await this.email.isPresent();
        this.email.clear();
    }

    async getTitle() {
        await this.title.isPresent();
        return this.title.getAttribute('jhiTranslate');
    }

    async save() {
        await this.saveButton.isPresent();
        return this.saveButton.click();
    }
}
