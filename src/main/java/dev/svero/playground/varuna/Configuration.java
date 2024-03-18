package dev.svero.playground.varuna;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implements a class for accessing the settings read from a
 * properties file.
 *
 * @author Sven Roeseler
 */
public class Configuration {
    final static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    final Properties properties = new Properties();

    /**
     * Tries to load the properties.
     */
    public boolean init(final String filename) {
        boolean result = true;

        InputStream inputStream;

        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            // Try to load from normal file
            try {
                inputStream = new FileInputStream(filename);
            } catch (FileNotFoundException ex) {
                LOGGER.error("File not found: {}", filename);
                throw new IllegalStateException("Specified file was not found: " + filename, ex);
            }
        } else {
            // Try to load from resource
            if (filename.startsWith("/")) {
                LOGGER.debug("Try to load configuration as classpath resource: {}", 
                    filename);
                inputStream = getClass().getResourceAsStream(filename);
            } else {
                String tmpFilename = "/" + filename;
                LOGGER.debug("Try to load configuration as classpath resource: {}", 
                    tmpFilename);
                // We need to prefix the path with "/" to find in within the resource path
                inputStream = getClass().getResourceAsStream(tmpFilename);
            }

            if (inputStream == null) {
                LOGGER.warn("Properties file not found");
                return false;
            }
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("An unspecified error occurred while reading the file", e);
            throw new IllegalStateException("Could not load data from file", e);
        }

        return result;
    }

    /**
     * Gets the value for the specified key as String. If the key is not found a value of null is returned.
     *
     * @param key Key
     * @return Value as string or null
     */
    public String getString(final String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key may not be blank");
        }

        return properties.getProperty(key);
    }

    /**
     * Gets the value for the specified key as String. If the key is not found and the flag "failWhenNotFound" is
     * set to true an IllegalStateException is thrown. Otherwise, null is returned.
     *
     * @param key Key to lookup for
     * @param failWhenNotFound If true an exception is thrown when the key was not found
     * @return Value as string or null
     * @throws IllegalStateException If failWhenNotFound is true and the key was not found
     */
    public String getString(final String key, final boolean failWhenNotFound) {
        final String result = getString(key);

        if (failWhenNotFound && result == null) {
            throw new IllegalStateException("Value for key \"" + key + "\" not found");
        }

        return result;
    }

    /**
     * Gets the value for the specified key as String. If the key is not found the specified default value
     * is returned.
     *
     * @param key Key
     * @param defaultValue Default value in case the key was not found
     * @return Value as String or the specified default value
     */
    public String getString(final String key, final String defaultValue) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key may not be blank");
        }

        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets the value of the specified key as integer value.
     *
     * @param key Key
     * @param defaultValue Default value if key was not found
     * @return Value
     */
    public int getInteger(final String key, final int defaultValue) {
        int value;

        String strValue = getString(key);
        if (StringUtils.isBlank(strValue)) {
            return defaultValue;
        }

        try {
            value = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            LOGGER.error("Error while reading integer value", e);
            return defaultValue;
        }

        return value;
    }
}
