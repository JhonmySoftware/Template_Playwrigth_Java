package com.example.project.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralizes configuration loading and allows Maven/system property overrides
 * without spreading resource access logic across the framework.
 */
public final class ConfigManager {
    private final Properties properties = new Properties();

    public ConfigManager(String resourceName) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Configuration resource not found: " + resourceName);
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load configuration resource: " + resourceName, exception);
        }
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required configuration key is missing: " + key);
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String resourceValue = properties.getProperty(key);
        if (resourceValue != null && !resourceValue.isBlank()) {
            return resourceValue.trim();
        }

        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, String.valueOf(defaultValue)));
    }

    public int getInt(String key, int defaultValue) {
        String rawValue = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer value for key: " + key + " -> " + rawValue, exception);
        }
    }
}
