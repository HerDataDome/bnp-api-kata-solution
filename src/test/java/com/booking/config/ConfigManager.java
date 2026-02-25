package com.booking.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager.
 * Loads environment-specific properties from src/test/resources/config/{env}.properties.
 * The target environment is controlled via the Maven system property: -Denv=test (default)
 *
 * Usage: ConfigManager.getInstance().getBaseUrl()
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        String env = System.getProperty("env", "test");
        String configFile = "config/" + env + ".properties";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new RuntimeException("Configuration file not found on classpath: " + configFile);
            }
            properties.load(input);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load configuration file: " + configFile, ex);
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    public String getAdminUsername() {
        return properties.getProperty("admin.username");
    }

    public String getAdminPassword() {
        return properties.getProperty("admin.password");
    }
}