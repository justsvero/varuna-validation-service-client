package dev.svero.playground.varuna;

import dev.svero.playground.varuna.utils.HttpUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Implements methods for accessing a KeyCloak server.
 *
 * @author Sven Roeseler
 */
public class KeyCloakClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloakClient.class);
    private static final String ACCESS_TOKEN_KEY = "access_token";

    private final HttpUtils httpUtils;
    private final String keyCloakBaseUrl;
    private final String keyCloakRealm;

    /**
     * Creates a new instance.
     *
     * @param httpUtils Instance of HttpUtils
     * @param keyCloakBaseUrl Base URL of the KeyCloak service (without trailing /)
     * @param keyCloakRealm Target realm at KeyCloak
     */
    public KeyCloakClient(HttpUtils httpUtils, String keyCloakBaseUrl, String keyCloakRealm) {
        this.httpUtils = httpUtils;
        this.keyCloakBaseUrl = keyCloakBaseUrl;
        this.keyCloakRealm = keyCloakRealm;
    }

    /**
     * Tries to get an access token for the specified signed JSON Web Token from KeyCloak.
     *
     * @param signedJsonWebToken Signed JSON Web Token
     * @return Created access token
     */
    public String getAccessToken(final String signedJsonWebToken) {
        String accessToken = null;

        String tokenRequestData = "grant_type=client_credentials" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=" + signedJsonWebToken;
        LOGGER.debug("Token request data: {}", tokenRequestData);

        final String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                keyCloakBaseUrl, keyCloakRealm);
        LOGGER.debug("Token request url: {}", url);

        String result;
        try {
            result = httpUtils.postRequest(url, tokenRequestData);
            LOGGER.debug("Raw result: {}", result);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            LOGGER.error("Could not successfully perform the HTTP request to KeyCloak", e);
            throw new RuntimeException("An error occurred while processing the request to KeyCloak", e);
        }

        JSONObject jsonObject = new JSONObject(result);
        if (jsonObject.has(ACCESS_TOKEN_KEY)) {
            accessToken = jsonObject.getString(ACCESS_TOKEN_KEY);
        } else {
            LOGGER.error("No access token found in received data!");
        }

        return accessToken;
    }
}
