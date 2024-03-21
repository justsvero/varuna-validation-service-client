package dev.svero.playground.varuna.utils;

import dev.svero.playground.varuna.exceptions.SSLUtilsException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.*;

/**
 * Implements methods for handling SSL/TLS.
 *
 * @author Sven Roeseler
 */
public class SSLUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLUtils.class);

    /**
     * Creates a SSL context.
     *
     * @return SSL context or null
     */
    public SSLContext createSSLContext(KeyStore keyStore, String keyStorePassword, KeyStore trustStore) {
        if (keyStore == null) {
            throw new IllegalArgumentException("keyStore should not be null");
        }

        if (StringUtils.isBlank(keyStorePassword)) {
            throw new IllegalArgumentException("keyStorePassword should not be empty");
        }

        if (trustStore == null) {
            throw new IllegalArgumentException("trustStore should not be null");
        }

        SSLContext context;

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            KeyManagerFactory kmf;
            try {
                kmf = KeyManagerFactory.getInstance("PKIX");
            } catch (NoSuchAlgorithmException ex) {
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Used algorithm for key manager factory: {}", kmf.getAlgorithm());
            }

            kmf.init(keyStore, keyStorePassword.toCharArray());

            context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new SSLUtilsException("Could not create SSL context instance", e);
        }

        return context;
    }
}
