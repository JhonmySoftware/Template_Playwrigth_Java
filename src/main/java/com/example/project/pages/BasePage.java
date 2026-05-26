package com.example.project.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Encapsulates low-level Playwright interactions shared by all page objects.
 * Tests should talk to business actions, not direct locator operations.
 */
public abstract class BasePage {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void navigate(String url, String pageName) {
        try {
            page.navigate(url);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            logger.info("Navigated to {}.", pageName);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Unable to navigate to " + pageName + ": " + url, exception);
        }
    }

    protected void fill(Locator locator, String value, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            locator.fill(value);
            logger.debug("Filled {}.", elementName);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Unable to fill " + elementName, exception);
        }
    }

    protected void click(Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            locator.click();
            logger.debug("Clicked {}.", elementName);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Unable to click " + elementName, exception);
        }
    }

    protected String textOf(Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            String text = locator.textContent();
            return text == null ? "" : text.trim();
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Unable to read text from " + elementName, exception);
        }
    }

    protected boolean isVisible(Locator locator) {
        try {
            return locator.isVisible();
        } catch (PlaywrightException exception) {
            logger.debug("Visibility check failed: {}", exception.getMessage());
            return false;
        }
    }

    protected void waitForUrlContains(String urlFragment, String pageName) {
        try {
            page.waitForURL(url -> url.contains(urlFragment));
            logger.info("Confirmed navigation to {}.", pageName);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Expected URL containing '" + urlFragment + "' for " + pageName, exception);
        }
    }
}
