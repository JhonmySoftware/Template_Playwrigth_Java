package com.example.project.pages;

import com.example.project.config.FrameworkConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the login screen. Locators favor data-testid and role-based
 * selectors because they are less brittle than CSS chained to layout.
 */
public final class LoginPage extends BasePage {
    private final FrameworkConfig frameworkConfig;
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    public LoginPage(Page page, FrameworkConfig frameworkConfig) {
        super(page);
        this.frameworkConfig = frameworkConfig;
        this.usernameInput = page.getByTestId("username");
        this.passwordInput = page.getByTestId("password");
        this.loginButton = page.getByTestId("login-button");
        this.errorMessage = page.getByTestId("error");
    }

    public LoginPage navigateToLoginPage() {
        navigate(frameworkConfig.getLoginPageUrl(), "login page");
        return this;
    }

    public LoginPage enterUsername(String username) {
        fill(usernameInput, username, "username input");
        return this;
    }

    public LoginPage enterPassword(String password) {
        fill(passwordInput, password, "password input");
        return this;
    }

    public LoginPage clickLogin() {
        click(loginButton, "login button");
        return this;
    }

    public LoginPage attemptLogin(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    public boolean isErrorMessageVisible() {
        return isVisible(errorMessage);
    }

    public InventoryPage loginExpectingSuccess(String username, String password) {
        attemptLogin(username, password);
        return new InventoryPage(page, frameworkConfig).waitUntilLoaded();
    }

    public String getErrorMessage() {
        return textOf(errorMessage, "error message");
    }
}
