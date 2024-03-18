package dev.svero.playground.varuna.utils;

import dev.svero.playground.varuna.exceptions.KeyStoreUtilsException;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Implements methods for handling key stores.
 *
 * @author Sven Roeseler
 */
public class KeyStoreUtils {
    /**
     * Tries to load a key store using the specified format.
     *
     * @param keyStoreFilename Filename of the key store
     * @param keyStorePassword Password for accessing the key store
     * @param keyStoreType Type (JKS, PKCS.12)
     * @return Created KeyStore instance
     */
    public KeyStore loadKeyStore(final String keyStoreFilename, final String keyStorePassword, final String keyStoreType) {
        if (StringUtils.isBlank(keyStoreFilename)) {
            throw new IllegalArgumentException("keyStoreFilename should not be blank");
        }

        if (StringUtils.isBlank(keyStorePassword)) {
            throw new IllegalArgumentException("keyStorePassword should not be blank");
        }

        if (StringUtils.isBlank(keyStoreType)) {
            return loadKeyStore(keyStoreFilename, keyStorePassword);
        }

        KeyStore keyStore = null;

        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreFilename), keyStorePassword.toCharArray());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new KeyStoreUtilsException("Could not create a KeyStore instance", e);
        }

        return keyStore;
    }

    /**
     * Tries to load a key store using the PKCS.12 format.
     *
     * @param keyStoreFilename Filename of the key store
     * @param keyStorePassword Password for accessing the key store
     * @return Created KeyStore instance
     */
    public KeyStore loadKeyStore(final String keyStoreFilename, final String keyStorePassword) {
        return loadKeyStore(keyStoreFilename, keyStorePassword, "PKCS12");
    }

    /**
     * Tries to get the specified private key.
     *
     * @param keyStore Key store instance
     * @param alias Alias for the private key entry
     * @param password Password for accessing the private key entry
     * @return Private key
     */
    public PrivateKey getKey(final KeyStore keyStore, final String alias, final String password) {
        if (keyStore == null) {
            throw new IllegalArgumentException("keyStore may not be null");
        }

        if (StringUtils.isBlank(alias)) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException();
        }

        PrivateKey privateKey;
        try {
            privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

        return privateKey;
    }
}
