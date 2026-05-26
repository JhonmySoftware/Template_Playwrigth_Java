package com.example.project.pages;

import com.example.project.config.FrameworkConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class InventoryPage extends BasePage {
    private final FrameworkConfig frameworkConfig;
    private final Locator pageTitle;

    public InventoryPage(Page page, FrameworkConfig frameworkConfig) {
        super(page);
        this.frameworkConfig = frameworkConfig;
        this.pageTitle = page.getByTestId("title");
    }

    public InventoryPage waitUntilLoaded() {
        waitForUrlContains(frameworkConfig.getInventoryUrlFragment(), "inventory page");
        return this;
    }

    public boolean isLoaded() {
        return isVisible(pageTitle);
    }

    public String getPageTitle() {
        return textOf(pageTitle, "inventory page title");
    }
}
