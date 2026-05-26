package com.example.project.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * Keeps browser creation decisions in one place so BaseTest remains focused on
 * lifecycle management and tests can switch browsers through configuration.
 */
public final class PlaywrightFactory {
    private final FrameworkConfig frameworkConfig;

    public PlaywrightFactory(FrameworkConfig frameworkConfig) {
        this.frameworkConfig = frameworkConfig;
    }

    public Playwright createPlaywright() {
        Playwright playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute(frameworkConfig.getTestIdAttribute());
        return playwright;
    }

    public Browser createBrowser(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(frameworkConfig.isHeadless())
                .setSlowMo((double) frameworkConfig.getSlowMo());

        String browserName = frameworkConfig.getBrowserName().toLowerCase();
        return switch (browserName) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            case "chromium" -> playwright.chromium().launch(options);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };
    }

    public BrowserContext createBrowserContext(Browser browser) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(frameworkConfig.getViewportWidth(), frameworkConfig.getViewportHeight());

        if (frameworkConfig.isVideoEnabled()) {
            options.setRecordVideoDir(frameworkConfig.getArtifactsBaseDirectory().resolve("raw-videos"))
                    .setRecordVideoSize(frameworkConfig.getVideoWidth(), frameworkConfig.getVideoHeight());
        }

        return browser.newContext(options);
    }
}
