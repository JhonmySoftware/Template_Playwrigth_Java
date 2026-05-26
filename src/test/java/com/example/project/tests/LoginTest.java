package com.example.project.tests;

import com.example.project.pages.InventoryPage;
import com.example.project.pages.LoginPage;
import com.example.project.utils.CsvDataReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public final class LoginTest extends BaseTest {
    private final CsvDataReader csvDataReader = new CsvDataReader();

    @DataProvider(name = "valid-login-data")
    public Object[][] validLoginData() {
        List<Map<String, String>> rows = csvDataReader.read("testdata/login-credentials.csv");
        return rows.stream()
                .map(row -> new Object[]{
                        row.get("username"),
                        row.get("password"),
                        row.get("expectedTitle")
                })
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "valid-login-data")
    public void shouldLoginSuccessfully(String username, String password, String expectedTitle) {
        LoginPage loginPage = new LoginPage(page, frameworkConfig);
        InventoryPage inventoryPage = loginPage.navigateToLoginPage()
                .loginExpectingSuccess(username, password);

        Assert.assertTrue(
                inventoryPage.isLoaded(),
                "Inventory page should be visible after a successful login."
        );
        Assert.assertTrue(
                page.url().contains(frameworkConfig.getInventoryUrlFragment()),
                "The browser should navigate to the inventory route after a successful login."
        );
        Assert.assertEquals(
                inventoryPage.getPageTitle(),
                expectedTitle,
                "Inventory title does not match the expected value."
        );
    }

    @Test
    public void shouldDisplayLockedOutErrorForRestrictedUser() {
        LoginPage loginPage = new LoginPage(page, frameworkConfig);

        loginPage.navigateToLoginPage()
                .attemptLogin("locked_out_user", "secret_sauce");

        Assert.assertTrue(
                loginPage.isErrorMessageVisible(),
                "Error banner should be visible for a locked out user."
        );
        Assert.assertEquals(
                loginPage.getErrorMessage(),
                "Epic sadface: Sorry, this user has been locked out.",
                "Locked out users should receive the expected business error."
        );
    }
}
