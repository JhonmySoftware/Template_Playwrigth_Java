package com.example.project.config;

import com.example.project.utils.ConfigManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exposes typed framework settings so tests and page objects do not parse raw
 * strings or know where configuration values come from.
 */
public final class FrameworkConfig {
    private final ConfigManager configManager;

    public FrameworkConfig() {
        this(new ConfigManager("config.properties"));
    }

    public FrameworkConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String getBrowserName() {
        return configManager.getProperty("browser", "chromium");
    }

    public boolean isHeadless() {
        return configManager.getBoolean("headless", false);
    }

    public int getSlowMo() {
        return configManager.getInt("slow.mo", 300);
    }

    public int getDefaultTimeoutMs() {
        return configManager.getInt("default.timeout.ms", 10000);
    }

    public int getNavigationTimeoutMs() {
        return configManager.getInt("navigation.timeout.ms", 15000);
    }

    public int getViewportWidth() {
        return configManager.getInt("viewport.width", 1440);
    }

    public int getViewportHeight() {
        return configManager.getInt("viewport.height", 900);
    }

    public String getBaseUrl() {
        return configManager.getRequiredProperty("base.url");
    }

    public String getTestIdAttribute() {
        return configManager.getProperty("test.id.attribute", "data-testid");
    }

    public boolean isTraceEnabled() {
        return configManager.getBoolean("trace.enabled", true);
    }

    public boolean isVideoEnabled() {
        return configManager.getBoolean("video.enabled", true);
    }

    public boolean isScreenshotOnFailureEnabled() {
        return configManager.getBoolean("screenshot.on.failure", true);
    }

    public boolean isScreenshotOnSuccessEnabled() {
        return configManager.getBoolean("screenshot.on.success", true);
    }

    public Path getArtifactsBaseDirectory() {
        return Paths.get(configManager.getProperty("artifacts.base.dir", "target/artifacts")).toAbsolutePath();
    }

    public Path getReportsBaseDirectory() {
        return Paths.get(configManager.getProperty("reports.base.dir", "target/reports")).toAbsolutePath();
    }

    public int getVideoWidth() {
        return configManager.getInt("video.width", 1280);
    }

    public int getVideoHeight() {
        return configManager.getInt("video.height", 720);
    }

    public String getInventoryUrlFragment() {
        return configManager.getProperty("inventory.url.fragment", "inventory.html");
    }

    public String getLoginPageUrl() {
        return getBaseUrl();
    }
}
